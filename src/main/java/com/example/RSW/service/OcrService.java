// ✅ [추가] Google Cloud Vision 기반 OCR 서비스
//    - 역할: 업로드 이미지 → GCV 호출 → { text, confidence:null } JSON(Map) 반환
//    - 장점: 컨트롤러를 얇게 유지, 추후 전처리/로깅/통계 확장 용이

package com.example.RSW.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// ⬇️ Google Cloud Vision SDK
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

@Service
public class OcrService {

	// ✅ OCR 모드 설정 (기본: 문서/영수증에 적합한 DOCUMENT_TEXT_DETECTION)
	@Value("${gcv.ocrMode:DOCUMENT_TEXT_DETECTION}")
	private String gcvOcrMode;

	/**
	 * 이미지에서 텍스트를 추출하여 React에서 쓰기 좋은 JSON(Map)으로 반환
	 * 
	 * @param file 업로드 이미지(Multipart)
	 * @return { "text": "...", "confidence": null }
	 * @throws Exception GCV 호출/IO 중 예외 발생 시 상위에서 처리
	 */
	public Map<String, Object> extractText(MultipartFile file) throws Exception {
		// 1) 업로드 파일 바이트 → GCV Image 변환
		ByteString imgBytes = ByteString.readFrom(file.getInputStream()); // 입력 스트림을 모두 읽어 바이트화
		Image img = Image.newBuilder().setContent(imgBytes).build(); // GCV Image 객체 생성

		// 2) OCR 모드 선택: TEXT_DETECTION(간판/짧은문구) vs DOCUMENT_TEXT_DETECTION(영수증/문서)
		Type type = "TEXT_DETECTION".equalsIgnoreCase(gcvOcrMode) ? Feature.Type.TEXT_DETECTION
				: Feature.Type.DOCUMENT_TEXT_DETECTION; // 기본: 문서형

		// 3) 기능(Feature) 지정 및 요청 생성
		Feature feat = Feature.newBuilder().setType(type).build();
		AnnotateImageRequest req = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();

		String text;

		// 4) GCV 클라이언트로 배치 요청(여기서는 단일 이미지만 전송)
		try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
			BatchAnnotateImagesResponse resp = client.batchAnnotateImages(java.util.List.of(req));
			AnnotateImageResponse r = resp.getResponses(0);

			// 5) 오류 처리
			if (r.hasError()) {
				throw new IllegalStateException("Vision OCR error: " + r.getError().getMessage());
			}

			// 6) 문서형이면 fullTextAnnotation.text, 일반형이면 textAnnotations[0].description 사용
			if (r.hasFullTextAnnotation()) {
				text = r.getFullTextAnnotation().getText();
			} else if (!r.getTextAnnotationsList().isEmpty()) {
				text = r.getTextAnnotations(0).getDescription();
			} else {
				text = "";
			}
		}

		// 7) React 친화 JSON(Map) 구성
		Map<String, Object> payload = new HashMap<>();
		payload.put("text", text != null ? text.trim() : "");
		payload.put("confidence", null); // 평균 신뢰도는 기본 응답에 별도 제공되지 않음(필요시 확장)
		return payload;
	}
}
