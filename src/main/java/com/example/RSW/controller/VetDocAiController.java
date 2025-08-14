package com.example.RSW.controller;

import com.example.RSW.service.VetDocAiService;
import com.example.RSW.vo.PrescriptionDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class VetDocAiController {

    private final VetDocAiService ai;

    /** ▶ 아무 JSON이나 넣으면 보호자 설명(Markdown) 반환 */
    @PostMapping("/explain")
    public Map<String, Object> explain(@RequestBody Object json,
                                       @RequestParam(defaultValue = "false") boolean detailed) {
        String md = ai.explainJson(json, detailed); // ← 상세 여부 전달
        return Map.of("markdown", md);
    }

    /** 헬스체크용 */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("ok", true);
    }

}
