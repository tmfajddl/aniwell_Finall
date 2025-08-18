// [추가파일] OCR 텍스트 → 날짜별 그룹 + 문서타입별 JSON 변환
package com.example.RSW.service;

import com.example.RSW.dto.OcrParseResponse;

import org.springframework.stereotype.Service;
import com.example.RSW.dto.OcrParseResponse.DocType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OCR 원문 텍스트를 날짜별로 묶고, 문서 타입별(items 필드)로 파싱합니다. - 기존 흐름은 건드리지 않고, 컨트롤러에서 호출만
 * 추가합니다.
 */
@Service
public class OcrFormatService {

	/** 엔트리포인트: OCR 원문 → JSON */
	public OcrParseResponse format(String rawText, DocType hint) {
		String text = normalize(rawText);

		// 1) 타입 감지(힌트 우선)
		DocType docType = (hint != null && hint != DocType.UNKNOWN) ? hint : detectDocType(text);

		// 2) 날짜 블록 분리
		List<Pair> blocks = splitByDate(text);
		if (blocks.isEmpty())
			blocks = List.of(new Pair("Unknown", text));

		// 3) 블록별 파싱
		List<OcrParseResponse.Group> groups = new ArrayList<>();
		for (Pair b : blocks) {
			List<Map<String, Object>> items = switch (docType) {
			case LAB -> parseLabItems(b.body);
			case RECEIPT -> parseReceiptItems(b.body);
			case PRESCRIPTION -> parsePrescriptionItems(b.body);
			case DIAGNOSIS -> parseDiagnosisItems(b.body);
			default -> parseLabItems(b.body);
			};
			groups.add(new OcrParseResponse.Group(b.date, items));
		}

		// 4) (옵션) 사람이 보기 쉬운 ASCII
		String ascii = buildAscii(groups, docType);

		return new OcrParseResponse(docType, groups, ascii);
	}

	// ---------- 내부 유틸 ----------

	private String normalize(String s) {
		if (s == null)
			return "";
		return s.replace("\\r\\n", "\n").replace("\\n", "\n").replace("\\r", "\n").replaceAll("\\u0000", "")
				.replaceAll("[ \\t]+", " ").trim();
	}

	private DocType detectDocType(String t) {
		String lc = t == null ? "" : t.toLowerCase(Locale.ROOT);
		if (lc.matches("(?s).*\\b(glucose|bun|crea|creatinine|검사결과|정상범위|u/l|mg/dl|g/dl)\\b.*"))
			return DocType.LAB;
		if (lc.matches("(?s).*\\b(영수증|합계|금액|단가|수량|vat)\\b.*"))
			return DocType.RECEIPT;
		if (lc.matches("(?s).*\\b(처방|복용|mg|tablet|1일\\s*\\d+\\s*회)\\b.*"))
			return DocType.PRESCRIPTION;
		if (lc.matches("(?s).*\\b(진단서|진단명|의사|병원|icd)\\b.*"))
			return DocType.DIAGNOSIS;
		return DocType.UNKNOWN;
	}

	// 날짜: yyyy-MM-dd 또는 yyyy-MM-dd HH:mm:ss
	private static final Pattern DATE_RE = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}(?:[ T]\\d{2}:\\d{2}:\\d{2})?)");

	private List<Pair> splitByDate(String text) {
		Matcher m = DATE_RE.matcher(text);
		List<Pair> out = new ArrayList<>();
		int lastIdx = 0;
		String cur = null;
		while (m.find()) {
			String d = m.group(1);
			if (cur != null)
				out.add(new Pair(cur, text.substring(lastIdx, m.start())));
			cur = d;
			lastIdx = m.end();
		}
		if (cur != null)
			out.add(new Pair(cur, text.substring(lastIdx)));
		return out;
	}

	// ---- 파서들 (라이트 버전: OCR 노이즈 고려, 규칙 점층 강화 가능) ----
	private List<Map<String, Object>> parseLabItems(String body) {
		List<Map<String, Object>> list = new ArrayList<>();
		for (String ln : body.split("\n")) {
			String line = ln.trim();
			if (line.isEmpty())
				continue;
			if (line.matches("(?i)^(검사명|정상범위|storage:|\\[?chemistry\\]?|\\[?hematology\\]?).*"))
				continue;

			String[] parts = line.split("→|=>|->");
			if (parts.length < 2)
				continue;

			String left = parts[0].trim();
			String right = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length)).trim();

			// 값(+플래그)
			String value = null, flag = null;
			Matcher vm = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)(?:\\s*\\((▲|▼|=)\\))?").matcher(right);
			if (vm.find()) {
				value = vm.group(1);
				flag = vm.group(2);
			} else {
				value = right.replaceAll("[^0-9.]", "");
			}

			// 단위 추정
			String unit = null;
			Matcher um = Pattern.compile("\\(([^()]+)\\)\\s*$").matcher(left);
			if (um.find())
				unit = um.group(1);

			// 레퍼런스 범위
			String ref = null;
			String name = left;
			Matcher rm = Pattern.compile("([\\-\\d.]+)\\s*~\\s*([\\-\\d.]+)|(~)").matcher(left);
			if (rm.find()) {
				if (rm.group(3) != null) {
					ref = "~";
					name = left.replace("~", "").trim();
				} else {
					ref = rm.group(1) + " ~ " + rm.group(2);
					name = left.replace(rm.group(0), "").trim();
				}
			}

			name = name.replaceAll("[:\\-–]+$", "").trim();
			if (name.isEmpty())
				continue;

			Map<String, Object> row = new LinkedHashMap<>();
			row.put("name", name);
			row.put("refRange", ref);
			row.put("unit", unit);
			row.put("value", value);
			row.put("flag", flag);
			list.add(row);
		}
		return list;
	}

	private List<Map<String, Object>> parseReceiptItems(String body) {
		List<Map<String, Object>> list = new ArrayList<>();
		for (String ln : body.split("\n")) {
			String line = ln.trim();
			if (line.isEmpty())
				continue;

			// 예: "X-ray 1 x 30000 = 30000"
			Matcher m = Pattern.compile("^(.+?)\\s+(\\d+)\\s*[xX*]\\s*(\\d[\\d,]*)\\s*=\\s*(\\d[\\d,]*)").matcher(line);
			Map<String, Object> row = new LinkedHashMap<>();
			if (m.find()) {
				row.put("item", m.group(1).trim());
				row.put("qty", m.group(2));
				row.put("unitPrice", m.group(3).replace(",", ""));
				row.put("total", m.group(4).replace(",", ""));
			} else {
				// 단순 "항목 금액"
				m = Pattern.compile("^(.+?)\\s+(\\d[\\d,]*)$").matcher(line);
				if (m.find()) {
					row.put("item", m.group(1).trim());
					row.put("qty", "1");
					row.put("unitPrice", m.group(2).replace(",", ""));
					row.put("total", m.group(2).replace(",", ""));
				} else {
					continue;
				}
			}
			list.add(row);
		}
		return list;
	}

	private List<Map<String, Object>> parsePrescriptionItems(String body) {
		List<Map<String, Object>> list = new ArrayList<>();
		for (String ln : body.split("\n")) {
			String line = ln.trim();
			if (line.isEmpty())
				continue;

			// 예: "아목시실린 500 mg 1일 3회 7일"
			Matcher m = Pattern.compile(
					"^(.+?)\\s+(\\d+(?:\\.\\d+)?)\\s*(mg|g|ml|iu|tablet|capsule)?\\s+(?:1일\\s*)?(\\d+)회\\s+(\\d+)일",
					Pattern.CASE_INSENSITIVE).matcher(line);

			Map<String, Object> row = new LinkedHashMap<>();
			if (m.find()) {
				row.put("drugName", m.group(1).trim());
				row.put("doseValue", m.group(2));
				row.put("doseUnit", m.group(3));
				row.put("freqPerDay", m.group(4));
				row.put("durationDays", m.group(5));
			} else {
				row.put("raw", line); // 규칙 미적용 라인 보존
			}
			list.add(row);
		}
		return list;
	}

	private List<Map<String, Object>> parseDiagnosisItems(String body) {
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("diagnosisText", body.trim());
		list.add(row);
		return list;
	}

	private String buildAscii(List<OcrParseResponse.Group> groups, DocType docType) {
		StringBuilder sb = new StringBuilder();
		for (OcrParseResponse.Group g : groups) {
			sb.append("# ").append(g.getDate()).append(" (").append(docType).append(")\n");
			for (Map<String, Object> r : g.getItems())
				sb.append(" - ").append(r).append("\n");
			sb.append("\n");
		}
		return sb.toString();
	}

	private record Pair(String date, String body) {
	}
}
