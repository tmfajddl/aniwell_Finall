// ✅ [추가] OCR 전용 컨트롤러
//    - 기능: 업로드된 이미지를 Google Cloud Vision(GCV)으로 OCR 수행 후 React 친화 JSON으로 반환
//    - 특징: 엔드포인트(/api/ocr/extract) 및 응답 포맷은 유지, 내부 OCR 엔진만 GCV로 교체
//    - 준비: (1) pom.xml에 google-cloud-vision 의존성 추가
//            (2) 환경변수 GOOGLE_APPLICATION_CREDENTIALS 로 서비스계정키(JSON) 경로 설정
//            (3) application.yml에 gcv.ocrMode 설정(선택, 기본 DOCUMENT_TEXT_DETECTION)

package com.example.RSW.api;

import java.util.HashMap; // 응답 payload(Map) 생성을 위함
import java.util.Map;

import org.springframework.beans.factory.annotation.Value; // application.yml 설정값 주입
import org.springframework.web.bind.annotation.PostMapping; // POST 엔드포인트 매핑
import org.springframework.web.bind.annotation.RequestMapping; // 컨트롤러 베이스 경로 매핑
import org.springframework.web.bind.annotation.RestController; // REST 컨트롤러 선언
import org.springframework.web.bind.annotation.RequestParam; // multipart 파라미터 바인딩
import org.springframework.web.multipart.MultipartFile; // 업로드 파일 수신

import com.example.RSW.vo.ResultData; // ✅ 프로젝트의 ResultData 경로에 맞게 유지(성공/실패 표준 응답)

// ⬇️ Google Cloud Vision SDK (GCV) 사용을 위한 임포트
import com.google.cloud.vision.v1.AnnotateImageRequest; // 이미지 요청 객체
import com.google.cloud.vision.v1.AnnotateImageResponse; // 단일 이미지 응답
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;// 배치 응답(여러 이미지)
import com.google.cloud.vision.v1.Feature; // 어떤 기능(TEXT_DETECTION 등) 사용할지
import com.google.cloud.vision.v1.Feature.Type; // Feature 타입 enum
import com.google.cloud.vision.v1.Image; // GCV용 이미지 객체
import com.google.cloud.vision.v1.ImageAnnotatorClient; // GCV 클라이언트
import com.google.protobuf.ByteString; // 바이트 컨테이너
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

// ⬇️ [보존용 주석] Tess4J 기반 사용 시 필요했던 임포트 (현재는 GCV 사용으로 미사용)
// import java.nio.file.Files;
// import java.nio.file.Path;
// import net.sourceforge.tess4j.ITesseract;
// import net.sourceforge.tess4j.Tesseract;

@RestController // JSON 기반 응답을 반환하는 컨트롤러임을 선언
@RequestMapping("/api/ocr") // 이 컨트롤러의 기본 URL prefix
public class OcrController {

	@Value("${gcv.credentials.json:}")
	private String gcvCredJson;

	@Value("${gcv.credentials.path:}")
	private String gcvCredPath;

	@Value("${gcv.credentials.base64:}")
	private String gcvCredBase64;

	@Autowired
	private ResourceLoader resourceLoader;

	// ✅ [보존] Tess4J용 설정(현재 GCV로 전환했지만, 추후 토글 시 재사용 가능)
	@Value("${tesseract.datapath:}") // tessdata 상위 경로(비워두면 OS 기본 경로 사용)
	private String tessDataPath;

	@Value("${tesseract.language:kor+eng}") // Tess4J 언어 설정(한국어+영어)
	private String tessLanguage;

	// ✅ [추가] GCV OCR 모드 주입(없으면 문서형 OCR로 동작)
	// - TEXT_DETECTION: 일반 사진(간판/짧은 글)
	// - DOCUMENT_TEXT_DETECTION: 영수증/문서(표/여러 줄 텍스트) 권장
	@Value("${gcv.ocrMode:DOCUMENT_TEXT_DETECTION}")
	private String gcvOcrMode;

	/**
	 * ✅ 영수증 이미지에서 텍스트를 추출하는 엔드포인트 - 요청: multipart/form-data; 필드명 "file" 에 이미지 파일 첨부
	 * - 응답: ResultData 표준 포맷 { "resultCode": "S-OK", "msg": "OCR 완료", "data": {
	 * "text": "...", "confidence": null } } ※ confidence는 GCV 기본 응답에 평균 신뢰도가 별도
	 * 제공되지 않아 null로 둠(필요 시 확장)
	 */
	@PostMapping("/extract") // POST /api/ocr/extract
	public ResultData<Map<String, Object>> extract(@RequestParam("file") MultipartFile file) {
		try {
			// 🔧 [추가] 업로드 파일 유효성 검사 (빈 파일 방지)
			if (file == null || file.isEmpty()) {
				return ResultData.from("F-OCR", "업로드된 파일이 없습니다.");
			}

			// --------------------------------------------------------------------
			// ⛔ [보존용 주석] Tess4J 호출 흐름 (현재 미사용. 향후 엔진 토글 시 참고)
			// Path tmp = Files.createTempFile("ocr_", "_" + file.getOriginalFilename()); //
			// 업로드 파일 임시 저장
			// Files.write(tmp, file.getBytes()); // 바이트 기록
			// ITesseract tess = new Tesseract(); // Tess4J 엔진 생성
			// if (tessDataPath != null && !tessDataPath.isBlank()) {
			// tess.setDatapath(tessDataPath); // tessdata 상위 경로 설정
			// }
			// tess.setLanguage(tessLanguage); // 언어 설정 (kor+eng)
			// String text = tess.doOCR(tmp.toFile()); // OCR 수행 → 전체 텍스트
			// --------------------------------------------------------------------

			// ✅ [실사용] Google Cloud Vision OCR 호출부
			// 1) 업로드 파일 바이트를 읽어 GCV Image 객체로 변환
			ByteString imgBytes = ByteString.readFrom(file.getInputStream()); // 입력 스트림 → 바이트
			Image img = Image.newBuilder().setContent(imgBytes).build(); // 바이트 → GCV Image

			// 2) OCR 모드 결정: 기본은 DOCUMENT_TEXT_DETECTION(영수증/문서에 유리)
			Type type = "TEXT_DETECTION".equalsIgnoreCase(gcvOcrMode) ? Feature.Type.TEXT_DETECTION
					: Feature.Type.DOCUMENT_TEXT_DETECTION;

			// 3) 어떤 기능(Feature)을 사용할지 지정하고 요청 객체 구성
			Feature feat = Feature.newBuilder().setType(type) // 선택한 OCR 모드 지정
					.build();
			AnnotateImageRequest req = AnnotateImageRequest.newBuilder().addFeatures(feat) // 기능 추가
					.setImage(img) // 대상 이미지 설정
					.build(); // 요청 객체 완성

			String text; // 최종 추출 텍스트를 담을 변수

			// 4) GCV 클라이언트를 생성해 배치 요청 실행(여러 장도 가능하나 여기선 1장만)
			// 🔧 [변경] 기본 create() → ADC(환경변수) 자격증명 명시 주입
			// - 전제: OS 환경변수 GOOGLE_APPLICATION_CREDENTIALS 에 서비스계정 JSON 경로 설정
			// 4) GCV 클라이언트를 생성해 배치 요청 실행(여러 장도 가능하나 여기선 1장만)
			// 🔧 [변경] gcv.credentials.* 우선 사용 → 없으면 ADC로 폴백
			Credentials creds = null;

			// 1) JSON 문자열 우선
			if (gcvCredJson != null && !gcvCredJson.isBlank()) {
				try (var in = new ByteArrayInputStream(gcvCredJson.getBytes(StandardCharsets.UTF_8))) {
					creds = ServiceAccountCredentials.fromStream(in); // 서비스계정 JSON 파싱
				}
			}
			// 2) BASE64 문자열
			else if (gcvCredBase64 != null && !gcvCredBase64.isBlank()) {
				byte[] decoded = java.util.Base64.getDecoder().decode(gcvCredBase64);
				try (var in = new ByteArrayInputStream(decoded)) {
					creds = ServiceAccountCredentials.fromStream(in);
				}
			}
			// 3) 경로(classpath:/ 또는 file:/)
			else if (gcvCredPath != null && !gcvCredPath.isBlank()) {
				Resource r = resourceLoader.getResource(gcvCredPath);
				try (var in = r.getInputStream()) {
					creds = ServiceAccountCredentials.fromStream(in);
				}
			}
			// 4) 마지막 수단: ADC(환경변수 GOOGLE_APPLICATION_CREDENTIALS)
			else {
				creds = GoogleCredentials.getApplicationDefault()
						.createScoped("https://www.googleapis.com/auth/cloud-platform");
			}

			ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
					.setCredentialsProvider(FixedCredentialsProvider.create(creds)).build();

			try (ImageAnnotatorClient client = ImageAnnotatorClient.create(settings)) {
				BatchAnnotateImagesResponse resp = client.batchAnnotateImages(java.util.List.of(req));
				AnnotateImageResponse r = resp.getResponses(0);

				if (r.hasError()) {
					throw new IllegalStateException("Vision OCR error: " + r.getError().getMessage());
				}

				if (r.hasFullTextAnnotation()) {
					text = r.getFullTextAnnotation().getText();
				} else if (!r.getTextAnnotationsList().isEmpty()) {
					text = r.getTextAnnotations(0).getDescription();
				} else {
					text = "";
				}
			}

			// 7) React에서 다루기 쉬운 JSON 스키마로 가공 (text + confidence)
			Map<String, Object> payload = new HashMap<>();
			payload.put("text", text != null ? text.trim() : ""); // 전체 텍스트(앞뒤 공백 정리)
			payload.put("confidence", null); // 평균 신뢰도는 별도 계산 시 확장 가능
			payload.put("mode", type.name()); // 🔧 [추가] 사용한 OCR 모드 확인용(개발 편의)

			// 8) 표준 성공 응답(ResultData)로 감싸서 반환
			// ⬇️ [유지/확인] 프로젝트의 ResultData 시그니처에 맞춰 data 키 사용
			return ResultData.from("S-OK", "OCR 완료", "data", payload);

		} catch (Exception e) {
			// 9) 예외 발생 시 스택트레이스 로깅 후 표준 실패 응답 반환
			e.printStackTrace();

			// 🔧 [추가] 실패 원인(간단)도 data에 포함 → Network 탭에서 즉시 확인 가능
			Map<String, Object> extra = new HashMap<>();
			extra.put("errorType", e.getClass().getSimpleName());
			extra.put("error", String.valueOf(e.getMessage()));

			// ⬇️ [변경] fail(...) 대신 from(..., "data", extra) 형태로 상세 전달
			return ResultData.from("F-OCR", "OCR 처리 중 오류가 발생했습니다.", "data", extra);
		}
	}

}
