// ✅ [신규] OCR 텍스트 저장을 위한 요청 VO (React JSON 바인딩용)
// - visitId가 없을 때 petId로 새 Visit를 만들고 문서를 붙입니다.

package com.example.RSW.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OcrSaveVo {
	// [필수] OCR 원문 텍스트
	private String text;

	// [선택] 기존 방문에 붙일 때 사용
	private Integer visitId;

	// [선택] 새 방문 생성에 필요한 기본 정보
	private Integer petId; // visitId 없으면 필요
	private LocalDateTime visitDate; // ISO-8601 문자열로 전송 시 Jackson이 바인딩
	private String hospital;
	private String doctor;
	private String diagnosis;
	private String notes;
	private String docType; // receipt | prescription | lab | diagnosis | other
	private String fileUrl; // 원본 이미지/파일 URL
}
