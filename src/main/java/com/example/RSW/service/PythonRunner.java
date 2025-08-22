package com.example.RSW.service;

import com.example.RSW.dto.LitterAnalysisDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PythonRunner {

    private final ObjectMapper om;

    @Value("${aniwell.python.exe}")
    private String pythonExe;

    @Value("${aniwell.python.script}")
    private String scriptPath;

    @Value("${aniwell.python.frames:6}")
    private int frames;

    @Value("${aniwell.python.model:gpt-4o}")
    private String model;

    /** true 이고 force=false 면 --ignore-empty 를 넘긴다 */
    @Value("${aniwell.python.ignoreEmpty:true}")
    private boolean ignoreEmpty;

    /** 프로세스 타임아웃(초) */
    @Value("${aniwell.python.timeoutSec:120}")
    private long timeoutSec;

    /** OPENAI_API_KEY 를 여기서 환경변수로 주입 */
    @Value("${openai.api-key:}")
    private String openaiApiKey;

    /* -------------------- Public API (오버로드) -------------------- */

    public LitterAnalysisDto runOnVideo(File video, Double lingerSec) throws Exception {
        return runOnVideo(video, lingerSec, false, null, false);
    }

    public LitterAnalysisDto runOnVideo(File video, Double lingerSec, boolean force) throws Exception {
        return runOnVideo(video, lingerSec, force, null, false);
    }

    public LitterAnalysisDto runOnVideo(
            File video,
            Double lingerSec,
            boolean force,
            Integer framesOverride,
            boolean squatOnly
    ) throws Exception {
        if (video == null || !video.exists()) {
            throw new FileNotFoundException("Video not found: " + video);
        }

        List<String> cmd = new ArrayList<>();
        cmd.add(pythonExe);
        cmd.add(scriptPath);
        cmd.add("--video");   cmd.add(video.getAbsolutePath());
        cmd.add("--frames");  cmd.add(String.valueOf(framesOverride != null ? framesOverride : this.frames));
        cmd.add("--model");   cmd.add(model);

        if (lingerSec != null) {
            cmd.add("--linger"); cmd.add(String.valueOf(lingerSec));
        }
        if (ignoreEmpty && !force) {
            cmd.add("--ignore-empty");
        }
        if (squatOnly) {
            cmd.add("--squat-only");
        }

        ProcessBuilder pb = new ProcessBuilder(cmd);
        if (openaiApiKey != null && !openaiApiKey.isBlank()) {
            pb.environment().put("OPENAI_API_KEY", openaiApiKey);
        }
        pb.redirectErrorStream(true);

        Process p = pb.start();

        String stdout;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            // 타임아웃 대기
            boolean finished = p.waitFor(timeoutSec, TimeUnit.SECONDS);
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) out.append(line).append("\n");
            stdout = out.toString().trim();

            if (!finished) {
                p.destroyForcibly();
                throw new RuntimeException("Python runner timeout (" + timeoutSec + "s)");
            }
        }

        // [Ignored] 로 시작하면 무시 케이스
        if (stdout.startsWith("[Ignored]")) {
            LitterAnalysisDto dto = new LitterAnalysisDto();
            dto.setIgnored(true);
            dto.setIgnoreReason(stdout);
            return dto;
        }

        if (stdout.isEmpty()) {
            throw new RuntimeException("Python returned empty output");
        }

        String json = extractFirstJson(stdout);
        return om.readValue(json, LitterAnalysisDto.class);
    }

    /* -------------------- Helpers -------------------- */

    private String extractFirstJson(String s) {
        int first = s.indexOf('{');
        int last  = s.lastIndexOf('}');
        if (first >= 0 && last >= first) {
            return s.substring(first, last + 1);
        }
        // 혹시 이미 JSON이면 그대로 반환
        return s;
    }
}
