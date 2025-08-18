package com.example.RSW.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * React 친화 JSON 응답용 DTO docType: 문서 유형(LAB/RECEIPT/PRESCRIPTION/DIAGNOSIS)
 * groups : 날짜별 그룹 [{ date, items[] }] ascii : 사람이 보기 쉬운 텍스트 표(옵션)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrParseResponse {
	private DocType docType;
	private List<Group> groups;
	private String ascii;

	public enum DocType {
		RECEIPT, PRESCRIPTION, LAB, DIAGNOSIS, UNKNOWN
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Group {
		private String date; // yyyy-MM-dd or yyyy-MM-dd HH:mm:ss
		private List<Map<String, Object>> items; // 문서타입별 동적 필드
	}

}
