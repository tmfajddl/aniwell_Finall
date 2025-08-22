package com.example.RSW.controller;

import com.example.RSW.dto.LitterAnalysisDto;
import com.example.RSW.dto.LitterAnalyzeResponse;
import com.example.RSW.vo.LitterEvent;
import com.example.RSW.repository.LitterEventRepository;
import com.example.RSW.service.IngestService;
import com.example.RSW.service.PythonRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/litter")
public class IngestController {

    private final IngestService ingestService;
    private final PythonRunner pythonRunner;
    private final LitterEventRepository litterEventMapper;
    private final ObjectMapper om;

    @Value("${aniwell.device.key:}")
    private String deviceKey;

    @Value("${aniwell.ingest.cleanup:true}")
    private boolean cleanupEnabled;

    private void assertDevice(String header) {
        if (StringUtils.hasText(deviceKey) && !deviceKey.equals(header)) {
            throw new RuntimeException("Unauthorized device");
        }
    }

    /** 프레임 1장 업로드: Content-Type: image/jpeg, raw body */
    @PostMapping(value = "/ingest/frame",
            consumes = { MediaType.IMAGE_JPEG_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE })
    public ResponseEntity<String> ingestFrame(
            @RequestParam String session,
            @RequestParam int seq,
            @RequestHeader(value = "X-Device-Key", required = false) String key,
            @RequestBody byte[] jpeg
    ) throws Exception {
        assertDevice(key);
        ingestService.saveJpegFrame(session, seq, jpeg);
        return ResponseEntity.ok("OK");
    }

    /** 세션 완료: 프레임을 mp4로 조립 → 분석 → (조건부) DB 저장 → 응답 */
    @PostMapping(value = "/ingest/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LitterAnalyzeResponse<LitterAnalysisDto>> complete(
            @RequestParam String session,
            @RequestParam Long petId,
            @RequestParam(defaultValue = "8") int fps,
            @RequestParam(value = "linger", required = false) Double linger,
            @RequestParam(value = "force", defaultValue = "false") boolean force,
            @RequestParam(value = "frames", required = false) Integer frames,
            @RequestParam(value = "squatOnly", defaultValue = "false") boolean squatOnly,
            @RequestHeader(value = "X-Device-Key", required = false) String key
    ) throws Exception {
        assertDevice(key);

        Path dir = ingestService.sessionDir(session);
        Path mp4 = ingestService.assembleMp4(session, fps);

        // Python 분석
        LitterAnalysisDto dto = pythonRunner.runOnVideo(mp4.toFile(), linger, force, frames, squatOnly);

        // 들렀다-나감이면 저장 스킵(원하면 저장하도록 변경 가능)
        if (!Boolean.TRUE.equals(dto.getIgnored())) {
            LitterEvent e = new LitterEvent();
            e.setPetId(petId);
            e.setDetectedAt(LocalDateTime.now());
            e.setType(dto.getType());
            e.setConfidence(dto.getConfidence());
            e.setVisualSignalsJson(om.writeValueAsString(dto.getVisual_signals()));
            e.setAnomaliesJson(om.writeValueAsString(dto.getAnomalies()));
            e.setNotes(dto.getNotes());
            e.setSourceVideo(mp4.getFileName().toString());
            e.setLogId(null);
            litterEventMapper.insert(e);
        }

        if (cleanupEnabled) {
            try { ingestService.cleanup(session); } catch (Exception ignored) {}
        }

        if (Boolean.TRUE.equals(dto.getIgnored())) {
            return ResponseEntity.ok(new LitterAnalyzeResponse<>("IGNORED", dto.getIgnoreReason(), dto));
        }
        return ResponseEntity.ok(new LitterAnalyzeResponse<>("OK", "분석완료", dto));
    }
}
