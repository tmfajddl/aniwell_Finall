// QrController.java
package com.example.RSW.controller;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.util.*;

@RestController
@RequestMapping("/api/qr")
public class QrController {

    @Value("${aniwell.qr.base:}")   // 스킴 포함된 절대 URL 권장 (예: https://.../qr.html)
    private String qrBase;

    // 디버그용: base/target 확인
    @GetMapping(value="/debug", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> debug(@RequestParam Map<String,String> all, HttpServletRequest req) {
        Map<String,String> q = new LinkedHashMap<>(all); q.remove("size");
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("base", resolveBase(req));
        out.put("params", q);
        out.put("target", buildTarget(resolveBase(req), q));
        return out;
    }

    @GetMapping(produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generate(
            @RequestParam Map<String,String> all,
            @RequestParam(defaultValue = "320") int size,
            HttpServletRequest req
    ) throws Exception {
        Map<String,String> q = new LinkedHashMap<>(all);
        q.remove("size");

        String base = resolveBase(req);
        String target = buildTarget(base, q);

        if (target == null || target.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("target url is empty".getBytes());
        }

        Map<EncodeHintType,Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.ERROR_CORRECTION,
                com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M);

        BitMatrix m = new MultiFormatWriter()
                .encode(target, BarcodeFormat.QR_CODE, size, size, hints);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(m, "PNG", baos);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.noCache())
                .body(baos.toByteArray());
    }

    // base 해석 (스킴 없거나 상대경로여도 동작)
    private String resolveBase(HttpServletRequest req) {
        String base = (qrBase == null) ? "" : qrBase.trim();
        if (base.isEmpty()) {
            // 폴백: 현재 서버의 /usr/pet/qr
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/usr/pet/qr").toUriString();
        }
        if (base.startsWith("http://") || base.startsWith("https://")) {
            return base;
        }
        // '/qr.html' 같은 상대 경로도 허용
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(base.startsWith("/") ? base : "/" + base)
                .toUriString();
    }

    private String buildTarget(String base, Map<String,String> q) {
        try {
            UriComponentsBuilder b = UriComponentsBuilder.fromUriString(base);
            q.forEach(b::queryParam);
            return b.build(true).toUriString();
        } catch (Exception e) {
            return null;
        }
    }
}
