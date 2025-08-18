package com.example.RSW.service;

import com.example.RSW.dto.ExplainRequest;
import com.example.RSW.vo.MedicalDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;

@Service
public class OcrNormalizer {

    private final ObjectMapper om = new ObjectMapper();


    /**
     * medical_document 한 건을 표준 ExplainRequest로 변환 (펫 제외).
     * - document.provider/date 는 ocr_json의 meta, 없으면 createdAt로 채움
     * - pet 은 비워둠 (Assembler에서 별도 주입)
     */
    public ExplainRequest fromDocument(MedicalDocument doc) {
        var document = new ExplainRequest.Document(providerFrom(doc), dateFrom(doc));
        var labs = new ArrayList<ExplainRequest.Lab>();
        var rxs  = new ArrayList<ExplainRequest.Prescription>();
        String notes;

        try {
            JsonNode root = (doc.getOcrJson()!=null && !doc.getOcrJson().isBlank())
                    ? om.readTree(doc.getOcrJson()) : om.createObjectNode();

            switch (safe(doc.getDocType())) {
                case "lab" -> {
                    // 기대 구조 예: {"labs":[{code,name,value,unit,ref_low,ref_high},...], "meta":{notes}}
                    if (root.has("labs") && root.get("labs").isArray()) {
                        for (JsonNode l : root.get("labs")) {
                            labs.add(new ExplainRequest.Lab(
                                    text(l.get("code")), text(l.get("name")),
                                    dbl(l.get("value")), text(l.get("unit")),
                                    dbl(l.get("ref_low")), dbl(l.get("ref_high"))
                            ));
                        }
                    }
                    notes = text(root.at("/meta/notes"));
                }
                case "prescription" -> {
                    // 예: {"prescriptions":[{drug,dose,dose_unit,freq,route,days},...]}
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
                    }
                    // 예: {"drugs":["세파클러","지르텍"]} 형태도 지원
                    else if (root.has("drugs") && root.get("drugs").isArray()) {
                        for (JsonNode n : root.get("drugs")) {
                            String drug = text(n);
                            if (!drug.isBlank()) {
                                rxs.add(new ExplainRequest.Prescription(drug, null, null, null, null, null));
                            }
                        }
                    }
                    notes = text(root.at("/meta/notes"));
                }
                case "receipt" -> {
                    // 예: {"items":["진료비","약값"], "total":45000, "meta":{"ts": "..."}}
                    String items = joinComma(root.get("items"));
                    String total = text(root.get("total"));
                    String ts    = text(root.at("/meta/ts"));
                    notes = buildReceiptNotes(items, total, ts);
                }
                default -> {
                    // 알 수 없는 타입은 원문을 notes로 보존
                    notes = (root.isMissingNode() || root.size()==0) ? null : ("원본 JSON: " + root.toString());
                }
            }
        } catch (Exception e) {
            notes = "정규화 오류: " + e.getMessage();
        }

        return new ExplainRequest(
                document,
                new ExplainRequest.Pet(null,null,null,null), // ← 펫은 비워둠
                labs,
                rxs,
                new ExplainRequest.Meta(notes)
        );
    }

    /* ----------------- helpers ----------------- */

    private String providerFrom(MedicalDocument doc) {
        try {
            JsonNode root = (doc.getOcrJson()!=null && !doc.getOcrJson().isBlank())
                    ? om.readTree(doc.getOcrJson()) : null;
            if (root != null) {
                String p = text(root.at("/meta/provider"));
                if (!p.isBlank()) return p;
            }
        } catch (Exception ignore) {}
        return "Aniwell Clinic"; // 기본값
    }

    private String dateFrom(MedicalDocument doc) {
        try {
            JsonNode root = (doc.getOcrJson()!=null && !doc.getOcrJson().isBlank())
                    ? om.readTree(doc.getOcrJson()) : null;
            if (root != null) {
                String d = text(root.at("/meta/date")); // "YYYY-MM-DD"
                if (!d.isBlank()) return d;
                String ts = text(root.at("/meta/ts"));  // "YYYY-MM-DDTHH:mm..."
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

    private static String safe(String s){ return s==null ? "" : s.toLowerCase(); }
    private static String text(JsonNode n){ return (n==null || n.isNull()) ? "" : n.asText(""); }
    private static Double dbl(JsonNode n){ return (n==null || n.isNull() || n.asText().isBlank()) ? null : n.asDouble(); }
    private static Integer intOrNull(JsonNode n){ return (n==null || n.isNull() || n.asText().isBlank()) ? null : n.asInt(); }
}
