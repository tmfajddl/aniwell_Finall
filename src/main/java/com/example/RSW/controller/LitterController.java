package com.example.RSW.controller;

import com.example.RSW.dto.LitterAnalysisDto;
import com.example.RSW.dto.LitterAnalyzeResponse;
import com.example.RSW.vo.LitterEvent;
import com.example.RSW.repository.LitterEventRepository;
import com.example.RSW.service.PythonRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/litter")
public class LitterController {

    private final PythonRunner pythonRunner;
    private final LitterEventRepository litterEventMapper;
    private final ObjectMapper objectMapper;

    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LitterAnalyzeResponse<LitterAnalysisDto>> analyze(
            @RequestPart("video") MultipartFile video,
            @RequestParam("petId") Long petId,                                 // ✅ 필수
            @RequestParam(value = "logId", required = false) Long logId,       // 선택
            @RequestParam(value = "sourceVideo", required = false) String src, // 선택(파일명/URL)
            @RequestParam(value = "linger", required = false) Double lingerSec,
            @RequestParam(value = "force", required = false, defaultValue = "false") boolean force
    ) throws Exception {

        File temp = File.createTempFile("aniwell_", "_" + video.getOriginalFilename());
        FileCopyUtils.copy(video.getBytes(), temp);

        try {
            // 파이썬 분석 호출 (force=true면 프리필터 무시)
            LitterAnalysisDto dto = pythonRunner.runOnVideo(temp, lingerSec, force);

            // 들렀다-나감 등 무시 케이스도 이벤트로 남기고 싶으면 if 조건 제거
            if (!Boolean.TRUE.equals(dto.getIgnored())) {
                LitterEvent e = new LitterEvent();
                e.setPetId(petId);
                e.setDetectedAt(LocalDateTime.now()); // 필요 시 logId의 logDate로 치환
                e.setType(dto.getType());
                e.setConfidence(dto.getConfidence());
                e.setVisualSignalsJson(objectMapper.writeValueAsString(dto.getVisual_signals()));
                e.setAnomaliesJson(objectMapper.writeValueAsString(dto.getAnomalies()));
                e.setNotes(dto.getNotes());
                e.setSourceVideo(src != null ? src : video.getOriginalFilename());
                e.setLogId(logId);
                litterEventMapper.insert(e);
            }

            if (Boolean.TRUE.equals(dto.getIgnored())) {
                return ResponseEntity.ok(new LitterAnalyzeResponse<>("IGNORED", dto.getIgnoreReason(), dto));
            }
            return ResponseEntity.ok(new LitterAnalyzeResponse<>("OK", "분석완료", dto));
        } finally {
            // 임시 파일 정리
            if (temp.exists()) temp.delete();
        }
    }

    // 헬스체크
    @GetMapping("/ping")
    public String ping() { return "pong"; }
}
