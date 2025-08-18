package com.example.RSW.service;

import com.example.RSW.dto.ExplainRequest;
import com.example.RSW.vo.MedicalDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * medical_document.ocr_json → 표준 ExplainRequest 정규화
 * - JSON(labs/prescriptions) 우선
 * - 텍스트만 있을 때 폴백 파서로 검사(lab) 추출
 * - 오입력된 doc_type(receipt 등)도 텍스트가 검사형이면 lab으로 전환
 */
@Service
public class OcrNormalizer {

    private final ObjectMapper om = new ObjectMapper();

    public ExplainRequest fromDocument(MedicalDocument doc) {
        var document = new ExplainRequest.Document(providerFrom(doc), dateFrom(doc));
        var labs = new ArrayList<ExplainRequest.Lab>();
        var rxs  = new ArrayList<ExplainRequest.Prescription>();
        String notes;

        try {
            JsonNode root = (doc.getOcrJson()!=null && !doc.getOcrJson().isBlank())
                    ? om.readTree(doc.getOcrJson()) : om.createObjectNode();

            String rawText = text(root.get("text"));
            String type = safe(doc.getDocType());

            // 텍스트 내용이 검사 형태면 강제로 lab 처리
            if (!"lab".equals(type) && looksLikeLabText(rawText)) type = "lab";

            switch (type) {
                case "lab" -> {
                    if (root.has("labs") && root.get("labs").isArray()) {
                        for (JsonNode l : root.get("labs")) {
                            labs.add(new ExplainRequest.Lab(
                                    text(l.get("code")), text(l.get("name")),
                                    dbl(l.get("value")), text(l.get("unit")),
                                    dbl(l.get("ref_low")), dbl(l.get("ref_high"))
                            ));
                        }
                    } else if (!rawText.isBlank()) {
                        labs.addAll(parseLabText(rawText));
                    }

                    notes = text(root.at("/meta/notes"));
                    if ((notes == null || notes.isBlank()) && !rawText.isBlank())
                        notes = "없음";
                }
                case "prescription" -> {
                    if (root.has("prescriptions") && root.get("prescriptions").isArray()) {
                        for (JsonNode p : root.get("prescriptions")) {
                            rxs.add(new ExplainRequest.Prescription(
                                    text(p.get("drug")),
                                    dbl(p.get("dose")),
                                    text(p.get("dose_unit")),
                                    text(p.get("freq")),
                                    text(p.get("route")),
                                    intOrNull(p.get("days"))
                            ));
                        }
                    } else if (root.has("drugs") && root.get("drugs").isArray()) {
                        for (JsonNode n : root.get("drugs")) {
                            String drug = text(n);
                            if (!drug.isBlank())
                                rxs.add(new ExplainRequest.Prescription(drug, null, null, null, null, null));
                        }
                    }
                    notes = text(root.at("/meta/notes"));
                }
                case "receipt" -> {
                    if (!rawText.isBlank() && looksLikeLabText(rawText)) {
                        labs.addAll(parseLabText(rawText));
                        notes = "영수증으로 저장되었으나 검사 텍스트로 자동 인식됨";
                        break;
                    }
                    String items = joinComma(root.get("items"));
                    String total = text(root.get("total"));
                    String ts    = text(root.at("/meta/ts"));
                    notes = buildReceiptNotes(items, total, ts);
                }
                default -> {
                    notes = (root.isMissingNode() || root.size()==0) ? null : ("원본 JSON: " + root.toString());
                }
            }
        } catch (Exception e) {
            notes = "정규화 오류: " + e.getMessage();
        }

        return new ExplainRequest(
                document,
                new ExplainRequest.Pet(null,null,null,null,null,null), // 펫은 어셈블러에서 주입
                labs, rxs, new ExplainRequest.Meta(notes)
        );
    }

    /* ==================== 텍스트 감지 ==================== */

    private boolean looksLikeLabText(String t) {
        if (t == null) return false;
        String s = t.toLowerCase(Locale.ROOT);
        if (s.contains("정상범위") || s.contains("reference")) return true;
        if (s.contains("u/l") || s.contains("mg/dl") || s.contains("g/dl") ||
                s.contains("pmol/l") || s.contains("μmol/l") || s.contains("mmhg") ||
                s.contains("mmol/l") || s.contains("(%)")) return true;
        return s.matches("(?s).*\\b(alt|ast|bun|crea|creatinine|ggt|bilirubin|glucose|cholesterol|albumin|amylase|lipase|sdma|fpl|ammonia|alkp|hct|hgb|po2|pco2|tco2|ph)\\b.*");
    }

    /* ==================== 폴백 라인 파서 ==================== */

    /**
     * 상태 기반 라인 파서
     * - name → (optional: 숫자, ~, 단위, 범위) → 값(▲/▼/=) ...
     * - 여러 값이 나오면 "마지막 값" 채택(최근 측정치 가정)
     */
    // OcrNormalizer.java 안의 기존 parseLabText(...) 를 아래로 전체 교체
    private List<ExplainRequest.Lab> parseLabText(String body) {
        var out = new ArrayList<ExplainRequest.Lab>();

        String curName = null;
        String unit = null;
        Double refLo = null, refHi = null;

        // ✨ 새로 추가: 줄 단위 임시 보관 (lo 후보)
        Double pendingLo = null;

        Pattern NAME = Pattern.compile("^[A-Za-z가-힣\\[\\]()+/\\-_.\\s]+$");
        Pattern RANGE_TILDE_UNIT = Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\s*~\\s*(-?\\d+(?:\\.\\d+)?)(?:\\s*\\(([^)]+)\\))?");
        Pattern RANGE_HYPHEN_UNIT = Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\s*-\\s*(-?\\d+(?:\\.\\d+)?)(?:\\s*\\(([^)]+)\\))?");
        Pattern RANGE_SPACE_UNIT  = Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\s+(-?\\d+(?:\\.\\d+)?)(?:\\s*\\(([^)]+)\\))?");
        Pattern UNIT_ONLY = Pattern.compile("^\\(([^)]+)\\)$");
        Pattern NUM_WITH_UNIT = Pattern.compile("^(-?\\d+(?:\\.\\d+)?)\\s*\\(([^)]+)\\)\\s*(?:\\(([▲▼=AV])\\))?$");
        Pattern JUST_NUM = Pattern.compile("^\\s*[-+]?\\d+(?:\\.\\d+)?\\s*$");

        for (String rawLine : body.split("\\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            // 헤더/잡음
            if (line.matches("(?i)^(검사명|정상범위|chemistry|hematology|storage:|urinalysis|hormone analysis).*$")) continue;
            if (line.equals("~") || line.equals("~ 0")) continue;

            // 1) 새 항목 이름
            if (!line.contains("~") && !line.matches(".*\\d.*") && NAME.matcher(line).matches()) {
                curName = line.replaceAll("[:\\-–]+$","").trim();
                unit = null; refLo = refHi = null; pendingLo = null;
                continue;
            }
            if (curName == null) continue;

            // 2) 범위 (lo~hi, lo-hi, "lo hi (unit)")
            Matcher m;
            m = RANGE_TILDE_UNIT.matcher(line);
            if (m.find()) {
                refLo = tryD(m.group(1)); refHi = tryD(m.group(2));
                if (m.group(3)!=null) unit = m.group(3).trim();
                pendingLo = null;
                continue;
            }
            m = RANGE_HYPHEN_UNIT.matcher(line);
            if (m.find()) {
                refLo = tryD(m.group(1)); refHi = tryD(m.group(2));
                if (m.group(3)!=null) unit = m.group(3).trim();
                pendingLo = null;
                continue;
            }
            m = RANGE_SPACE_UNIT.matcher(line);
            if (m.find() && line.split("\\s+").length<=4) {
                refLo = tryD(m.group(1)); refHi = tryD(m.group(2));
                if (m.group(3)!=null) unit = m.group(3).trim();
                pendingLo = null;
                continue;
            }

            // 3) 단위만 한 줄
            Matcher um = UNIT_ONLY.matcher(line);
            if (um.find()) {
                unit = um.group(1).trim();
                continue;
            }

            // 4-a) 숫자만 한 줄 → ✨ lo 후보로 임시 저장
            if (JUST_NUM.matcher(line).matches() && refLo == null && refHi == null) {
                pendingLo = tryD(line);
                continue;
            }

            // 4-b) "숫자 (단위)" 한 줄
            Matcher nu = NUM_WITH_UNIT.matcher(line);
            if (nu.find()) {
                Double num = tryD(nu.group(1));
                String u = nu.group(2).trim();
                // ✨ 직전이 숫자만 한 줄이었다면 → [pendingLo, num]을 참고범위로 확정
                if (pendingLo != null && refLo == null && refHi == null) {
                    refLo = pendingLo;
                    refHi = num;
                    unit = u;
                    pendingLo = null;
                    continue; // 값으로 추가하지 않고 범위만 세팅
                }
                // 그 외에는 '값'으로 간주
                unit = (unit==null ? u : unit);
                addOrReplace(out, curName, num, unit, refLo, refHi);
                continue;
            }

            // 5) 값 라인 (1.02(▼), 262(A), *2+(200), !(500) 등)
            Value v = extractValue(line);
            if (v != null) {
                addOrReplace(out, curName, v.num, unit, refLo, refHi);
                // 값이 들어갔으면 pendingLo는 의미 없으니 클리어
                pendingLo = null;
            }
        }

        return out;
    }


    /** 결과 목록에서 동일 항목 이름이 이미 있으면 "마지막 값"으로 교체 */
    private void addOrReplace(List<ExplainRequest.Lab> out, String name, Double val, String unit,
                              Double refLo, Double refHi) {
        if (val == null && unit == null && refLo == null && refHi == null) return;
        int idx = -1;
        for (int i=out.size()-1;i>=0;i--) {
            if (Objects.equals(out.get(i).getName(), name)) { idx = i; break; }
        }
        var row = new ExplainRequest.Lab(name, name, val, unit, refLo, refHi);
        if (idx >= 0) out.set(idx, row); else out.add(row);
    }

    /** *2+(200), !(500), 1.02(▼), 262(A), -9.3(▼) 등에서 숫자/플래그 추출 */
    private Value extractValue(String line) {
        // 1) 괄호 속 숫자가 있으면 그걸 우선 사용 (UA: *2+(200), !(500) 등)
        Matcher parenNum = Pattern.compile(".*\\(([-+]?\\d+(?:\\.\\d+)?)\\)\\s*(?:\\(([▲▼=AV])\\))?$").matcher(line);
        if (parenNum.matches()) {
            Double n = tryD(parenNum.group(1));
            String f = normalizeFlag(parenNum.group(2));
            return new Value(n, f);
        }
        // 2) 숫자 + (플래그)
        Matcher numFlag = Pattern.compile("^\\s*([-+]?\\d+(?:\\.\\d+)?)\\s*\\(([▲▼=AV])\\)\\s*$").matcher(line);
        if (numFlag.matches()) {
            return new Value(tryD(numFlag.group(1)), normalizeFlag(numFlag.group(2)));
        }
        // 3) 숫자만 있는 라인
        Matcher justNum = Pattern.compile("^\\s*([-+]?\\d+(?:\\.\\d+)?)\\s*$").matcher(line);
        if (justNum.matches()) {
            return new Value(tryD(justNum.group(1)), null);
        }
        return null;
    }

    private static String normalizeFlag(String s){
        if (s == null) return null;
        s = s.trim();
        if ("A".equalsIgnoreCase(s)) return "▲";
        if ("V".equalsIgnoreCase(s)) return "▼";
        return s;
    }

    private record Value(Double num, String flag) {}

    private static Double tryD(String s){
        try { return s==null? null : Double.valueOf(s); } catch(Exception e){ return null; }
    }

    /* ==================== 메타/공용 ==================== */

    private String providerFrom(MedicalDocument doc) {
        try {
            JsonNode root = (doc.getOcrJson()!=null && !doc.getOcrJson().isBlank())
                    ? om.readTree(doc.getOcrJson()) : null;
            if (root != null) {
                String p = text(root.at("/meta/provider"));
                if (!p.isBlank()) return p;
            }
        } catch (Exception ignore) {}
        return "Aniwell Clinic";
    }

    private String dateFrom(MedicalDocument doc) {
        try {
            JsonNode root = (doc.getOcrJson()!=null && !doc.getOcrJson().isBlank())
                    ? om.readTree(doc.getOcrJson()) : null;
            if (root != null) {
                String d = text(root.at("/meta/date"));
                if (!d.isBlank()) return d;
                String ts = text(root.at("/meta/ts"));
                if (!ts.isBlank()) return ts.length()>=10 ? ts.substring(0,10) : ts;
            }
        } catch (Exception ignore) {}
        return (doc.getCreatedAt()!=null) ? doc.getCreatedAt().toLocalDate().toString() : LocalDate.now().toString();
    }

    private static String joinComma(JsonNode arr){
        if (arr != null && arr.isArray()) {
            var list = new ArrayList<String>();
            arr.forEach(n -> list.add(text(n)));
            return String.join(", ", list);
        }
        return null;
    }

    private static String buildReceiptNotes(String items, String total, String ts){
        StringBuilder sb = new StringBuilder("영수증");
        if (ts != null && !ts.isBlank()) sb.append(" (").append(ts).append(")");
        sb.append(" · ");
        if (items != null && !items.isBlank()) sb.append("항목: ").append(items).append(" · ");
        sb.append("총액: ").append(total != null && !total.isBlank() ? total : "-");
        return sb.toString();
    }

    private static String safe(String s){ return s==null ? "" : s.toLowerCase(Locale.ROOT); }
    private static String text(JsonNode n){ return (n==null || n.isNull()) ? "" : n.asText(""); }
    private static Double dbl(JsonNode n){ return (n==null || n.isNull() || n.asText().isBlank()) ? null : n.asDouble(); }
    private static Integer intOrNull(JsonNode n){ return (n==null || n.isNull() || n.asText().isBlank()) ? null : n.asInt(); }
}
