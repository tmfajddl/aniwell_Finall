package com.example.RSW.service;

import com.example.RSW.dto.OcrParseResponse;
import com.example.RSW.dto.OcrParseResponse.DocType;
import com.example.RSW.dto.OcrParseResponse.Group;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OCR 텍스트 → 날짜별 그룹 + 문서타입별 JSON 변환
 * - LAB: 라인 병합/범위/단위 처리 + 멀티-날짜 헤더 분배
 * - RECEIPT/PRESCRIPTION: 간단 파서
 * - DIAGNOSIS: 전용 파서(병원/의사/진단/증상/치료/예후/그밖의사항 → notes, 방문일 추출)
 */
@Service
public class OcrFormatService {

	// ====== 공개 엔트리포인트 ======
	public OcrParseResponse format(String rawText, DocType hint) {
		String text = normalize(rawText);
		DocType docType = (hint != null && hint != DocType.UNKNOWN) ? hint : detectDocType(text);

		// ✅ 진단서: 전용 파서로 한 번에 그룹 구성
		if (docType == DocType.DIAGNOSIS) {
			Group g = buildDiagnosisGroup(text);
			List<Group> groups = Collections.singletonList(g);
			return new OcrParseResponse(docType, groups, buildAscii(groups, docType));
		}

		// LAB: 멀티-날짜 헤더 라인 감지(한 줄에 날짜 ≥2개면 열 순서로 분배)
		List<String> headerDates = detectHeaderDates(text);
		if (docType == DocType.LAB && headerDates.size() >= 2) {
			List<Map<String,Object>> items = parseLabItems(text);
			items = postFixLabItems(items);
			items = reconcileByUnitDominance(items);
			List<Group> groups = distributeByDates(items, headerDates);
			return new OcrParseResponse(docType, groups, buildAscii(groups, docType));
		}

		// 기본: 날짜로 블록 분리 → 타입별 파서 적용
		List<Pair> blocks = splitByDate(text);
		if (blocks.isEmpty()) blocks = Collections.singletonList(new Pair("Unknown", text));

		List<Group> groups = new ArrayList<>();
		for (Pair b : blocks) {
			List<Map<String, Object>> items;
			switch (docType) {
				case LAB:
					items = parseLabItems(b.body);
					items = postFixLabItems(items);
					items = reconcileByUnitDominance(items);
					break;
				case RECEIPT:
					items = parseReceiptItems(b.body);
					break;
				case PRESCRIPTION:
					items = parsePrescriptionItems(b.body);
					break;
				case DIAGNOSIS: // 폴백(위 전용 파서가 기본)
				default:
					items = parseLabItems(b.body);
					break;
			}
			groups.add(new Group(b.date, items));
		}
		return new OcrParseResponse(docType, groups, buildAscii(groups, docType));
	}

	// ====== 공통 유틸 ======
	private String normalize(String s) {
		if (s == null) return "";
		return s.replace("\\r\\n", "\n")
				.replace("\\n", "\n")
				.replace("\\r", "\n")
				.replace('\t', ' ')
				.replace('\u00A0', ' ')
				.trim();
	}

	public DocType suggestDocTypeEnum(String text) {
		DocType dt = detectDocType(text);
		return (dt == null) ? DocType.UNKNOWN : dt;
	}
	public String suggestDocType(String text) {
		DocType dt = detectDocType(text);
		return (dt == null || dt == DocType.UNKNOWN) ? null : dt.name().toLowerCase(Locale.ROOT);
	}

	private DocType detectDocType(String t) {
		String lc = t == null ? "" : t.toLowerCase(Locale.ROOT);
		if (lc.matches("(?s).*\\b(glucose|bun|crea|creatinine|검사결과|정상범위|mmol/l|mg/dl|g/dl|u/l)\\b.*"))
			return DocType.LAB;
		if (lc.matches("(?s).*\\b(영수증|합계|금액|단가|수량|vat|부가세)\\b.*"))
			return DocType.RECEIPT;
		if (lc.matches("(?s).*\\b(처방|복용|mg|tablet|캡슐|1일\\s*\\d+\\s*회)\\b.*"))
			return DocType.PRESCRIPTION;
		if (lc.matches("(?s).*\\b(진단서|진단명|의사|병원|icd)\\b.*"))
			return DocType.DIAGNOSIS;
		return DocType.UNKNOWN;
	}

	// yyyy-MM-dd 또는 yyyy-MM-dd HH:mm:ss
	private static final Pattern DATE_RE = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}(?:[ T]\\d{2}:\\d{2}:\\d{2})?)");

	private List<Pair> splitByDate(String text) {
		Matcher m = DATE_RE.matcher(text);
		List<Pair> out = new ArrayList<>();
		int lastIdx = 0;
		String cur = null;
		while (m.find()) {
			String d = m.group(1);
			if (cur != null) out.add(new Pair(cur, text.substring(lastIdx, m.start())));
			cur = d;
			lastIdx = m.end();
		}
		if (cur != null) out.add(new Pair(cur, text.substring(lastIdx)));
		return out;
	}

	// ====== LAB 전처리: 줄 병합(사람 눈처럼) ======
	private List<String> normalizeForLab(String text) {
		if (text == null) return Collections.emptyList();
		text = text.replace('\t', ' ')
				.replace('\r', '\n')
				.replace('\u00A0', ' ')
				.replace('−', '-')
				.replace('–', '-');

		List<String> raw = new ArrayList<>();
		for (String ln : text.split("\n")) {
			ln = ln.trim();
			if (!ln.isEmpty()) raw.add(ln);
		}
		if (raw.isEmpty()) return raw;

		List<String> out = new ArrayList<>();
		Pattern UNIT_ONLY     = Pattern.compile("^[~\\s]*\\(([^)]+)\\)\\s*$"); // "(mmHg)"
		Pattern ONLY_TILDE    = Pattern.compile("^~$|^~\\s*0$");
		Pattern NUM_ONLY      = Pattern.compile("^-?\\d+(?:\\.\\d+)?$");
		Pattern NUM_WITH_UNIT = Pattern.compile("^-?\\d+(?:\\.\\d+)?\\s*\\([^)]*\\)$");
		Pattern TWO_NUM_SPACE = Pattern.compile("^(-?\\d+(?:\\.\\d+)?)\\s+(-?\\d+(?:\\.\\d+)?)(?:\\s*\\(([^)]+)\\))?$");

		for (int i = 0; i < raw.size(); i++) {
			String cur = raw.get(i);

			// 단위 전용 줄 → 직전 줄에 부착
			Matcher mu = UNIT_ONLY.matcher(cur);
			if (mu.matches() && !out.isEmpty()) {
				out.set(out.size() - 1, out.get(out.size() - 1) + " (" + mu.group(1) + ")");
				continue;
			}

			// "~" 줄: 앞뒤 숫자를 병합
			if (ONLY_TILDE.matcher(cur).matches()
					&& !out.isEmpty()
					&& NUM_ONLY.matcher(out.get(out.size() - 1)).matches()
					&& i + 1 < raw.size()) {
				String prev = out.remove(out.size() - 1);
				String next = raw.get(++i).trim();
				out.add(prev + " ~ " + next);
				continue;
			}

			// "숫자" 다음 "숫자(단위)" → "lo ~ hi (unit)"
			if (!out.isEmpty() && NUM_ONLY.matcher(out.get(out.size() - 1)).matches()
					&& NUM_WITH_UNIT.matcher(cur).matches()) {
				String prev = out.remove(out.size() - 1);
				out.add(prev + " ~ " + cur);
				continue;
			}

			// "숫자 숫자 (단위)" → "lo ~ hi (unit)"
			Matcher m2 = TWO_NUM_SPACE.matcher(cur);
			if (m2.matches()) {
				String lo = m2.group(1);
				String hi = m2.group(2);
				String u  = (m2.groupCount() >= 3) ? m2.group(3) : null;
				out.add(u == null ? (lo + " ~ " + hi) : (lo + " ~ " + hi + " (" + u + ")"));
				continue;
			}

			// 5.5-80 → 5.5 ~ 80
			cur = cur.replaceAll("(?<=\\d)-(\\d)", " ~ $1");

			out.add(cur);
		}
		return out;
	}

	// ====== LAB 파서 ======
	private static final Pattern P_RANGE         = Pattern.compile("^(-?\\d+(?:\\.\\d+)?)\\s*~\\s*(-?\\d+(?:\\.\\d+)?)(?:\\s*\\(([^)]+)\\))?$");
	private static final Pattern P_VAL_WITH_MARK = Pattern.compile("^\\*?(-?\\d+(?:\\.\\d+)?)\\s*\\(([^)]+)\\)$"); // 1.02(▼), 128(=)
	private static final Pattern P_NUM_ONLY      = Pattern.compile("^-?\\d+(?:\\.\\d+)?$");

	private List<Map<String, Object>> parseLabItems(String body) {
		List<Map<String, Object>> list = new ArrayList<>();
		List<String> lines = normalizeForLab(body);

		Pattern P_NUM_WITH_UNIT   = Pattern.compile("^-?\\d+(?:\\.\\d+)?\\s*\\(([^)]+)\\)$");
		Pattern P_UA_GRADE        = Pattern.compile("^\\*?([0-4]\\+|trace|Tr)\\s*\\((-?\\d+(?:\\.\\d+)?)\\)$", Pattern.CASE_INSENSITIVE);
		Pattern P_SECTION_HEADERS = Pattern.compile("(?i)^(검사명|정상범위|urinalysis|hormone analysis|chemistry|hematology|cbc)$");

		Deque<String> nameQ = new ArrayDeque<>();
		String curName = null;
		Double refLo = null, refHi = null;
		String unit = null;

		boolean currentHasRealValue = false;

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i).trim();
			if (line.isEmpty() || P_SECTION_HEADERS.matcher(line).matches()) continue;

			boolean looksLikeName =
					!P_RANGE.matcher(line).matches() &&
							!P_VAL_WITH_MARK.matcher(line).matches() &&
							!P_NUM_ONLY.matcher(line).matches() &&
							!P_NUM_WITH_UNIT.matcher(line).matches() &&
							!(line.startsWith("(") && line.endsWith(")")) &&
							line.matches(".*[A-Za-z가-힣].*") &&
							!line.matches(".*\\d{4}-\\d{2}-\\d{2}.*");

			if (looksLikeName) {
				// 다음 10줄 안에 숫자가 안 나오면 잡음으로 무시
				if (!lookAheadHasRangeOrValue(lines, i, 10)) continue;

				// 현재 항목이 실값을 이미 냈다면 항목 닫기
				if (curName != null && currentHasRealValue) {
					curName = null;
					refLo = refHi = null;
					unit = null;
					currentHasRealValue = false;
				}
				// 이름은 큐에 쌓고 숫자 나오면 바인딩
				nameQ.addLast(line.replaceAll("[:\\-–]+$","").trim());
				continue;
			}

			// 숫자/범위 등장 → 이름 바인딩
			if (curName == null) {
				if (!nameQ.isEmpty()) {
					curName = nameQ.pollFirst();
					refLo = refHi = null; unit = null;
					currentHasRealValue = false;
				} else {
					continue; // 이름 없이 숫자만 → 버림
				}
			}

			// 참고범위
			Matcher mr = P_RANGE.matcher(line);
			if (mr.matches()) {
				if (currentHasRealValue && !nameQ.isEmpty()) {
					curName = nameQ.pollFirst();
					refLo = refHi = null;
					unit = null;
					currentHasRealValue = false;
				}
				refLo = tryD(mr.group(1));
				refHi = tryD(mr.group(2));
				if (mr.group(3) != null) unit = mr.group(3).trim();
				continue;
			}

			// 요검 반정량 (*2+(200) 등)
			Matcher mu = P_UA_GRADE.matcher(line);
			if (mu.matches()) {
				String note = mu.group(1).toUpperCase(Locale.ROOT);
				Double val  = tryD(mu.group(2));
				addRow(list, curName, val, unit, refLo, refHi, null, note);
				currentHasRealValue = true;
				continue;
			}

			// 숫자(▲/▼/=)
			Matcher mv = P_VAL_WITH_MARK.matcher(line);
			if (mv.matches()) {
				Double val = tryD(mv.group(1));
				String mark = mv.group(2).trim();
				String flag = "H";
				if ("▼".equals(mark)) flag = "L";
				else if ("=".equals(mark)) flag = "N";
				else if (!"▲".equals(mark)) flag = null;
				addRow(list, curName, val, unit, refLo, refHi, flag, (!"▲▼=".contains(mark) ? mark : null));
				currentHasRealValue = true;
				continue;
			}

			// 숫자만
			if (P_NUM_ONLY.matcher(line).matches()) {
				addRow(list, curName, tryD(line), unit, refLo, refHi, null, null);
				currentHasRealValue = true;
			}
		}

		return list;
	}

	private boolean lookAheadHasRangeOrValue(List<String> lines, int fromIdx, int window){
		for (int k=1; k<=window && fromIdx+k < lines.size(); k++){
			String s = lines.get(fromIdx+k).trim();
			if (s.isEmpty()) continue;
			if (P_RANGE.matcher(s).matches() ||
					P_VAL_WITH_MARK.matcher(s).matches() ||
					P_NUM_ONLY.matcher(s).matches()) return true;
		}
		return false;
	}

	// ====== LAB 후처리 ======
	private List<Map<String,Object>> postFixLabItems(List<Map<String,Object>> in) {
		if (in == null) return Collections.emptyList();
		List<Map<String,Object>> out = new ArrayList<>();
		String lastName = null;

		Pattern UNIT_IN_NAME    = Pattern.compile("^(.*)\\s*\\(([^)]+)\\)\\s*$");
		Pattern NAME_TILDE_UNIT = Pattern.compile("^~\\s*\\(([^)]+)\\)\\s*$");
		Pattern TRAILING_RANGE  = Pattern.compile(".*?~\\s*(-?\\d+(?:\\.\\d+)?)\\s+(-?\\d+(?:\\.\\d+)?)(?:\\s*\\(([^)]+)\\))?\\s*$");

		for (Map<String,Object> row : in) {
			String name = s(row.get("name"));
			String unit = s(row.get("unit"));
			Double val  = d(row.get("value"));
			Double lo   = d(row.get("ref_low"));
			Double hi   = d(row.get("ref_high"));

			// 이름 끝 "(단위)" → unit
			if ((unit == null || unit.isBlank()) && name != null) {
				Matcher m = UNIT_IN_NAME.matcher(name);
				if (m.matches()) {
					name = m.group(1).trim();
					unit = m.group(2).trim();
				}
			}

			// "~ (단위)" → 직전 항목명
			if (name != null) {
				Matcher m = NAME_TILDE_UNIT.matcher(name);
				if (m.matches() && lastName != null) {
					name = lastName;
					if (unit == null || unit.isBlank()) unit = m.group(1).trim();
				}
			}

			// 이름에 범위 섞임 → 직전 항목명으로 치환 + 범위 회수
			if (name != null && name.contains("~") && val != null && lastName != null) {
				Matcher mr = TRAILING_RANGE.matcher(name);
				if (mr.matches()) {
					lo = d(mr.group(1));
					hi = d(mr.group(2));
					if ((unit == null || unit.isBlank()) && mr.group(3) != null) unit = mr.group(3).trim();
					name = lastName;
				}
			}

			row.put("name", name);
			row.put("unit", unit == null ? "" : unit);
			row.put("value", val);
			row.put("ref_low", lo);
			row.put("ref_high", hi);
			row.put("refRange", buildRefRange(lo, hi));

			out.add(row);
			if (name != null && !name.isBlank()) lastName = name;
		}
		return out;
	}

	// ====== 단위 계열 기반 재배치 ======
	private enum UnitFamily { NONE, PERCENT, MG_DL, MMOL_L, MMHG, G_DL, COUNT_PER_uL, OTHER }

	private UnitFamily unitFamilyOf(String unit) {
		if (unit == null) return UnitFamily.NONE;
		String u = unit.trim().toLowerCase(Locale.ROOT);
		if (u.contains("%")) return UnitFamily.PERCENT;
		if (u.contains("mg/dl")) return UnitFamily.MG_DL;
		if (u.contains("mmol/l")) return UnitFamily.MMOL_L;
		if (u.contains("mmhg")) return UnitFamily.MMHG;
		if (u.contains("g/dl")) return UnitFamily.G_DL;
		if (u.contains("/µl") || u.contains("/ul") || u.contains("10x9/l") || u.contains("10x3/")) return UnitFamily.COUNT_PER_uL;
		if (u.isEmpty()) return UnitFamily.NONE;
		return UnitFamily.OTHER;
	}

	private List<Map<String,Object>> reconcileByUnitDominance(List<Map<String,Object>> items) {
		if (items == null || items.isEmpty()) return Collections.emptyList();

		List<UnitFamily> fam = new ArrayList<>(items.size());
		for (Map<String,Object> r : items) {
			fam.add(unitFamilyOf(String.valueOf(r.getOrDefault("unit",""))));
		}

		Map<String, Map<UnitFamily, Integer>> hist = new LinkedHashMap<>();
		for (int i=0;i<items.size();i++){
			String name = String.valueOf(items.get(i).getOrDefault("name",""));
			UnitFamily f = fam.get(i);
			Map<UnitFamily, Integer> hm = hist.get(name);
			if (hm == null) {
				hm = new EnumMap<>(UnitFamily.class);
				hist.put(name, hm);
			}
			Integer cnt = hm.get(f);
			hm.put(f, (cnt == null ? 1 : cnt + 1));
		}

		Map<String, UnitFamily> dominant = new LinkedHashMap<>();
		for (Map.Entry<String, Map<UnitFamily, Integer>> e : hist.entrySet()){
			UnitFamily best = UnitFamily.NONE; int bc = -1;
			for (Map.Entry<UnitFamily,Integer> h : e.getValue().entrySet()){
				if (h.getKey() == UnitFamily.NONE) continue;
				if (h.getValue() > bc){ bc = h.getValue(); best = h.getKey(); }
			}
			dominant.put(e.getKey(), (bc>=0 ? best : UnitFamily.NONE));
		}

		final int WINDOW = 6;
		for (int i=0; i<items.size(); i++){
			Map<String,Object> row = items.get(i);
			String curName = String.valueOf(row.getOrDefault("name",""));
			UnitFamily f = fam.get(i);
			UnitFamily dom = dominant.getOrDefault(curName, UnitFamily.NONE);

			if (f == UnitFamily.NONE || f == dom) continue;

			String bestName = null; int bestDist = Integer.MAX_VALUE;

			// 이전 탐색
			for (int j=i-1; j>=0 && i-j <= WINDOW; j--){
				String nm = String.valueOf(items.get(j).getOrDefault("name",""));
				UnitFamily domJ = dominant.getOrDefault(nm, UnitFamily.NONE);
				if (domJ == f) { bestName = nm; bestDist = i-j; break; }
			}
			// 다음 탐색
			for (int j=i+1; j<items.size() && j-i <= WINDOW; j++){
				String nm = String.valueOf(items.get(j).getOrDefault("name",""));
				UnitFamily domJ = dominant.getOrDefault(nm, UnitFamily.NONE);
				if (domJ == f && (j-i) < bestDist) { bestName = nm; bestDist = j-i; break; }
			}

			if (bestName != null && !bestName.equals(curName)) {
				row.put("name", bestName);
			}
		}
		return items;
	}

	// ====== 멀티-날짜 지원 ======
	private List<String> detectHeaderDates(String text){
		List<String> out = new ArrayList<>();
		if (text == null) return out;
		String[] lines = text.split("\\r?\\n");
		Pattern DAY = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
		for (String ln : lines){
			Matcher m = DAY.matcher(ln);
			List<String> found = new ArrayList<>();
			while (m.find()) found.add(m.group(1));
			if (found.size() >= 2) { out.addAll(found); break; }
		}
		return out;
	}

	private List<Group> distributeByDates(List<Map<String,Object>> items, List<String> dates){
		List<List<Map<String,Object>>> buckets = new ArrayList<>();
		for (int i=0;i<dates.size();i++) buckets.add(new ArrayList<>());

		for (int i=0; i<items.size(); ){
			String name = String.valueOf(items.get(i).getOrDefault("name",""));
			int j = i;
			List<Map<String,Object>> chunk = new ArrayList<>();
			while (j < items.size() &&
					name.equals(String.valueOf(items.get(j).getOrDefault("name","")))) {
				if (items.get(j).get("value") != null) chunk.add(items.get(j));
				j++;
			}
			for (int k=0; k<chunk.size() && k<dates.size(); k++) {
				buckets.get(k).add(chunk.get(k));
			}
			i = j;
		}

		List<Group> groups = new ArrayList<>();
		for (int d=0; d<dates.size(); d++){
			groups.add(new Group(dates.get(d), buckets.get(d)));
		}
		return groups;
	}

	// ====== 기타 문서 타입 ======
	private List<Map<String, Object>> parseReceiptItems(String body) {
		List<Map<String, Object>> list = new ArrayList<>();
		for (String ln : body.split("\n")) {
			String line = ln.trim();
			if (line.isEmpty()) continue;

			Matcher m = Pattern.compile("^(.+?)\\s+(\\d+)\\s*[xX*]\\s*(\\d[\\d,]*)\\s*=\\s*(\\d[\\d,]*)").matcher(line);
			Map<String, Object> row = new LinkedHashMap<>();
			if (m.find()) {
				row.put("item", m.group(1).trim());
				row.put("qty", m.group(2));
				row.put("unitPrice", m.group(3).replace(",", ""));
				row.put("total", m.group(4).replace(",", ""));
			} else {
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
			if (line.isEmpty()) continue;

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
				row.put("raw", line);
			}
			list.add(row);
		}
		return list;
	}

    /* =========================================================
       ✅ 진단서 파서 (병원/의사/진단명/증상/예후/치료/비고 + 날짜)
       ========================================================= */

	// 날짜 패턴(“2024. 4. 8.” / “2024년 4월 8일” 등)
	private static final Pattern P_DATE_DOT = Pattern.compile("(20\\d{2})[.년]\\s*(\\d{1,2})[.월]\\s*(\\d{1,2})[.일]?");
	private static final Pattern P_DATE_ANY = Pattern.compile("(20\\d{2})[.년\\-\\/]\\s*(\\d{1,2})[.월\\-\\/]\\s*(\\d{1,2})");

	private Group buildDiagnosisGroup(String full) {
		// 1) 방문일: '진단 연월일' 우선, 없으면 문서 내 첫 날짜(헤더 '개정/별지' 라인은 제외)
		String visitDate = firstDate(full, "진단\\s*연월일", "진단연월일");
		if (visitDate == null) visitDate = firstDate(full);

		// 2) 필드 추출 (멀티라인 허용)
		String hospital = coalesce(
				findAfterBlock(full, 1, "동물병원\\s*명칭", "병원명", "의료기관\\s*명", "기관명"),
				findAfter(full, "동물병원\\s*명칭", "병원명", "의료기관\\s*명", "기관명")
		);
		String address  = coalesce(
				findAfterBlock(full, 1, "동물병원\\s*주소", "주소", "소재지"),
				findAfter(full, "동물병원\\s*주소", "주소", "소재지")
		);
		String phone    = coalesce(
				findAfterBlock(full, 1, "전화번호", "Tel", "TEL", "연락처"),
				findAfter(full, "전화번호", "Tel", "TEL", "연락처")
		);
		String doctor   = coalesce(
				findAfterBlock(full, 1, "수의사\\s*성명", "의사\\s*성명", "담당\\s*수의사", "담당의"),
				findAfter(full, "수의사\\s*성명", "의사\\s*성명", "담당\\s*수의사", "담당의")
		);

		String diagnosis = coalesce(
				findAfterBlock(full, 2, "병명", "진단명"),
				findAfterBlock(full, 2, "임상적\\s*추정", "최종\\s*진단"),
				findAfter(full, "병명", "진단명", "임상적\\s*추정", "최종\\s*진단")
		);

		String symptoms  = coalesce(
				findAfterBlock(full, 3, "주요\\s*증상", "증상"),
				findAfter(full, "주요\\s*증상", "증상")
		);
		String therapy   = coalesce(
				findAfterBlock(full, 3, "치료명칭", "치료내용", "치료"),
				findAfter(full, "치료명칭", "치료내용", "치료")
		);
		String prognosis = coalesce(
				findAfterBlock(full, 3, "예후\\s*소견", "예후"),
				findAfter(full, "예후\\s*소견", "예후")
		);
		// ✅ notes = '그 밖의 사항/비고/특이사항'
		String etc       = coalesce(
				findAfterBlock(full, 4, "그\\s*밖의\\s*사항", "비고", "특이사항"),
				findAfter(full, "그\\s*밖의\\s*사항", "비고", "특이사항")
		);

		List<Map<String,Object>> items = new ArrayList<>();
		addKV(items, "hospital",  hospital);
		addKV(items, "address",   address);   // JSON에는 남김(컨트롤러에서 notes 만들 때 제외 가능)
		addKV(items, "phone",     phone);
		addKV(items, "doctor",    doctor);
		addKV(items, "diagnosis", diagnosis);
		addKV(items, "symptoms",  symptoms);
		addKV(items, "therapy",   therapy);
		addKV(items, "prognosis", prognosis);
		addKV(items, "notes",     etc);       // ✅ ‘그 밖의 사항’이 여기로

		if (items.isEmpty()) {
			Map<String,Object> row = new LinkedHashMap<>();
			row.put("key","diagnosisText");
			row.put("value", full == null ? "" : full.trim());
			items.add(row);
		}
		return new Group(visitDate != null ? visitDate : "Unknown", items);
	}

	private static void addKV(List<Map<String,Object>> items, String k, String v){
		if (v != null && !v.trim().isEmpty()) {
			Map<String,Object> row = new LinkedHashMap<>();
			row.put("key", k);
			row.put("value", v.trim());
			items.add(row);
		}
	}

	private boolean isBlank(String s){ return s==null || s.trim().isEmpty(); }

	/** 한 줄 라벨 뒤 값을 찾는다. (라벨: 값  /  라벨 값) */
	private String findAfter(String text, String... labels){
		if (text == null) return null;
		String[] lines = text.split("\\r?\\n");
		for (String lb : labels){
			Pattern p = Pattern.compile("^\\s*(?:" + lb + ")\\s*[:：]?\\s*(.+)\\s*$", Pattern.CASE_INSENSITIVE);
			for (String ln : lines){
				Matcher m = p.matcher(ln.trim());
				if (m.find()){
					String v = trimAtCutpoints(m.group(1));   // ★ 변경
					if (!v.isEmpty()) return v;
				}
			}
		}
		return null;
	}



	// 멀티라인 블록 값 추출(라벨 라인 + 다음 N줄, 새 섹션/날짜 나오면 중단)
	// 새 섹션 시작(다음 줄에서 끊기 위한 패턴) – 기존 그대로 유지
	private static final Pattern P_NEW_SECTION = Pattern.compile(
			"^\\s*(병명|진단명|임상적\\s*추정|최종\\s*진단|발병\\s*연월일|진단\\s*연월일|주요\\s*증상|치료명칭|치료내용|입원[·\\.]?퇴원일|예후\\s*소견|그\\s*밖의\\s*사항|비고|특이사항|동물병원\\s*명칭|동물병원\\s*주소|수의사\\s*면허번호|수의사\\s*성명|참고사항)\\b.*",
			Pattern.CASE_INSENSITIVE
	);

	private String findAfterBlock(String text, int carryLines, String... labels) {
		if (text == null) return null;
		String[] lines = text.split("\\r?\\n");

		for (String lb : labels) {
			// 라벨은 '라인 시작'에서만 인식
			Pattern head = Pattern.compile("^\\s*(?:" + lb + ")\\s*[:：]?\\s*(.*)$", Pattern.CASE_INSENSITIVE);

			for (int i = 0; i < lines.length; i++) {
				Matcher mh = head.matcher(lines[i]);
				if (!mh.find()) continue;

				StringBuilder sb = new StringBuilder();

				// 같은 줄 뒷부분에서 라벨/날짜 만나면 그 앞까지만
				String first = trimAtCutpoints(mh.group(1));  // ★ 변경
				if (!first.isEmpty()) sb.append(first);

				for (int j = i + 1; j < lines.length && j <= i + carryLines; j++) {
					String nxt = lines[j].trim();
					if (nxt.isEmpty()) break;
					if (P_NEW_SECTION.matcher(nxt).matches()) break;

					String cut = trimAtCutpoints(nxt);        // ★ 변경
					if (cut.isEmpty()) break;

					// 날짜만 덩그러니 → 섹션 경계로 간주
					if (toIsoDate(cut) != null && cut.length() <= 12) break;

					if (sb.length() > 0) sb.append(' ');
					sb.append(cut);
				}

				String out = sb.toString().trim();
				if (!out.isEmpty()) return out;
			}
		}
		return null;
	}



	private boolean containsIgnoreSpace(String line, String label){
		if (line == null || label == null) return false;
		String a = line.replaceAll("[\\s:：]", "");
		String b = label.replaceAll("\\s", "");
		return a.contains(b);
	}

	private String coalesce(String... vals){
		if (vals == null) return null;
		for (String v : vals) if (v != null && !v.trim().isEmpty()) return v.trim();
		return null;
	}

	/** 라벨 주변/문서 전체에서 yyyy.mm.dd, yyyy년 m월 d일 → ISO */
	private String firstDate(String text, String... nearLabels){
		if (text == null) return null;
		String[] lines = text.split("\\r?\\n");

		// 라벨 주변(해당 줄/다음 줄/다다음 줄)
		if (nearLabels != null && nearLabels.length > 0) {
			for (int i = 0; i < lines.length; i++) {
				String ln = lines[i];
				for (String lb : nearLabels) {
					if (containsIgnoreSpace(ln, lb)) {
						String iso = toIsoDate(ln);
						if (iso == null && i + 1 < lines.length) iso = toIsoDate(lines[i + 1]);
						if (iso == null && i + 2 < lines.length) iso = toIsoDate(lines[i + 2]);
						if (iso != null) return iso;
					}
				}
			}
		}
		// 전체 스캔: 헤더(개정/별지) 라인은 제외
		for (String ln : lines) {
			String t = ln.trim();
			if (t.contains("개정") || t.contains("별지")) continue;
			String iso = toIsoDate(t);
			if (iso != null) return iso;
		}
		return null;
	}

	private String toIsoDate(String s){
		if (s == null) return null;
		Matcher m = P_DATE_DOT.matcher(s);
		if (m.find()) {
			return String.format("%s-%02d-%02d",
					m.group(1), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
		}
		m = P_DATE_ANY.matcher(s);
		if (m.find()) {
			return String.format("%s-%02d-%02d",
					m.group(1), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
		}
		return null;
	}

	/* ====== (폴백) 진단서 Raw 보관 ====== */
	private List<Map<String, Object>> parseDiagnosisItems(String body) {
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("diagnosisText", body == null ? "" : body.trim());
		list.add(row);
		return list;
	}

	// ====== 보조 헬퍼 ======
	public OcrParseResponse keepLatestGroup(OcrParseResponse resp) {
		if (resp == null || resp.getGroups() == null || resp.getGroups().isEmpty()) return resp;
		Group best = null;
		long bestScore = Long.MIN_VALUE;
		for (Group g : resp.getGroups()) {
			long epoch = parseDateEpoch(g.getDate());
			boolean hasItems = g.getItems() != null && !g.getItems().isEmpty();
			long score = epoch + (hasItems ? 1_000_000_000_000L : 0L);
			if (score > bestScore) { bestScore = score; best = g; }
		}
		if (best == null) return resp;
		return new OcrParseResponse(resp.getDocType(), Collections.singletonList(best), resp.getAscii());
	}

	public List<Map<String, Object>> dedupeLatestPerTest(List<Map<String, Object>> items) {
		if (items == null) return Collections.emptyList();
		Map<String, Map<String,Object>> last = new LinkedHashMap<>();
		for (Map<String,Object> it : items) {
			String key = String.valueOf(it.getOrDefault("name", ""));
			last.put(key, it);
		}
		return new ArrayList<>(last.values());
	}

	private long parseDateEpoch(String s) {
		try {
			java.time.LocalDateTime dt;
			if (s != null && s.trim().length() > 10) {
				dt = java.time.LocalDateTime.parse(s.trim().replace(' ', 'T'));
			} else {
				dt = java.time.LocalDate.parse(s.trim()).atStartOfDay();
			}
			return dt.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
		} catch (Exception e) {
			return Long.MIN_VALUE + 1;
		}
	}

	private static void addRow(List<Map<String, Object>> list, String name, Double val, String unit,
							   Double lo, Double hi, String flag, String note) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("name", name);
		row.put("value", val);
		row.put("unit", unit == null ? "" : unit);
		row.put("ref_low", lo);
		row.put("ref_high", hi);
		row.put("refRange", buildRefRange(lo, hi));
		if (flag != null) row.put("flag", flag);
		if (note != null) row.put("note", note);
		list.add(row);
	}

	private static String buildRefRange(Double lo, Double hi) {
		String dash = "—";
		String L = (lo == null) ? dash : stripTrailingZero(lo);
		String H = (hi == null) ? dash : stripTrailingZero(hi);
		if (lo == null && hi == null) return dash;
		return L + " ~ " + H;
	}

	private static String stripTrailingZero(Double d) {
		if (d == null) return "";
		String s = Double.toString(d);
		if (s.endsWith(".0")) return s.substring(0, s.length() - 2);
		return s;
	}

	private static Double tryD(String s) {
		try { return s == null ? null : Double.valueOf(s); }
		catch (Exception e) { return null; }
	}
	private static Double d(Object o){ try { return o==null? null : Double.valueOf(String.valueOf(o)); } catch(Exception e){ return null; } }
	private static String s(Object o){ return o==null? null : String.valueOf(o); }

	private String buildAscii(List<Group> groups, DocType docType) {
		StringBuilder sb = new StringBuilder();
		for (Group g : groups) {
			sb.append("# ").append(g.getDate()).append(" (").append(docType).append(")\n");
			for (Map<String, Object> r : g.getItems()) sb.append(" - ").append(r).append("\n");
			sb.append("\n");
		}
		return sb.toString();
	}

	// Java 8 호환용 Pair
	private static class Pair {
		final String date;
		final String body;
		Pair(String d, String b){ this.date = d; this.body = b; }
	}

	// 라벨 토큰(다음 항목 시작 후보)
	private static final Pattern P_ANY_LABEL = Pattern.compile(
			"(동물병원\\s*명칭|동물병원\\s*주소|전화번호|수의사\\s*면허번호|수의사\\s*성명|의사\\s*성명|담당\\s*수의사|담당의|" +
					"병명|진단명|임상적\\s*추정|최종\\s*진단|발병\\s*연월일|진단\\s*연월일|주요\\s*증상|치료명칭|치료내용|" +
					"입원[·\\.]?퇴원일|예후\\s*소견|그\\s*밖의\\s*사항|비고|특이사항|참고사항)\\s*[:：]?",
			Pattern.CASE_INSENSITIVE
	);

	// 같은 줄 안에서 '2024년 6' / '2024. 6' / '2024-06' 같은 날짜 토큰 시작점
	private static final Pattern P_DATE_TOKEN = Pattern.compile(
			"20\\d{2}\\s*[.년\\-/]\\s*\\d{1,2}(?:\\s*[.월\\-/]\\s*\\d{1,2})?",
			Pattern.CASE_INSENSITIVE
	);

	// 라벨/날짜가 나오면 그 앞에서 자르고, 끝에 덩그러니 남은 괄호/구두점을 정리
	private String trimAtCutpoints(String s){
		if (s == null) return null;
		int cut = s.length();
		Matcher m1 = P_ANY_LABEL.matcher(s);
		if (m1.find()) cut = Math.min(cut, m1.start());
		Matcher m2 = P_DATE_TOKEN.matcher(s);
		if (m2.find()) cut = Math.min(cut, m2.start());
		String t = s.substring(0, cut).trim();
		// 끝에 남은 "(", "[", "{" 또는 구두점 정리
		t = t.replaceAll("[\\s·•ㆍ:：\\-–—]*[\\(\\[\\{]?$", "").trim();
		return t;
	}

}
