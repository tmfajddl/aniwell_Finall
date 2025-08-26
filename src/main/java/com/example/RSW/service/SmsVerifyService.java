package com.example.RSW.service;

import com.example.RSW.vo.ResultData;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsVerifyService {

    // === 설정 값 (application.yml에 정의) ===
    @Value("${solapi.api-key}")
    private String apiKey;

    @Value("${solapi.api-secret}")
    private String apiSecret;

    @Value("${solapi.domain:https://api.solapi.com}")
    private String domain;

    @Value("${solapi.from}") // 솔라피 콘솔에 등록/승인된 발신번호(숫자만)
    private String from;

    // === 정책 상수 (EmailVerificationService 스타일) ===
    private static final Duration CODE_TTL = Duration.ofMinutes(5);   // 코드 유효시간
    private static final Duration COOLDOWN = Duration.ofSeconds(60);  // 재전송 쿨다운

    // === 인프라 ===
    private final RedisTemplate<String, String> redis; // 프로젝트에서 이미 쓰는 타입과 동일
    private DefaultMessageService messageService;

    @PostConstruct
    void init() {
        messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, domain);
    }

    // === 키 규칙 ===
    private String keyCode(String phone) { return "sms:verify:code:" + phone; }
    private String keyCooldown(String phone) { return "sms:verify:cooldown:" + phone; }

    // 숫자만 남기기
    private String normalize(String rawPhone) {
        return rawPhone == null ? "" : rawPhone.replaceAll("\\D", "");
    }

    // 코드 해시(원문 코드는 저장하지 않음)
    private String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(d.length * 2);
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 인증코드 전송 */
    public ResultData<?> sendCode(String rawPhone) {
        final String phone = normalize(rawPhone);
        if (phone.isBlank()) {
            return ResultData.from("F-PHONE", "전화번호를 입력하세요.");
        }

        // 재전송 쿨다운 체크
        String cdKey = keyCooldown(phone);
        if (Boolean.TRUE.equals(redis.hasKey(cdKey))) {
            Long remain = redis.getExpire(cdKey, TimeUnit.SECONDS);
            long retryAfter = Math.max(1, remain == null ? 0 : remain);
            return ResultData.from("F-COOLDOWN", "잠시 후 다시 시도해 주세요.",
                    "retryAfterSec", retryAfter);
        }

        // 6자리 코드 생성
        String code = String.format("%06d", (int)(Math.random() * 1_000_000));

        // 코드 해시 저장(원문 미보관): code + phone
        String hash = sha256(code + "|" + phone);
        redis.opsForValue().set(keyCode(phone), hash, CODE_TTL.getSeconds(), TimeUnit.SECONDS);

        // 쿨다운 시작
        redis.opsForValue().set(cdKey, "1", COOLDOWN.getSeconds(), TimeUnit.SECONDS);

        // 문자 발송
        Message msg = new Message();
        msg.setFrom(from);
        msg.setTo(phone);
        msg.setText("[Aniwell] 인증번호 " + code + " (유효 " + CODE_TTL.toMinutes() + "분)");

        try {
            messageService.send(msg);
            return ResultData.from("S-OK", "인증번호를 전송했습니다.",
                    "ttlSec", CODE_TTL.getSeconds(),
                    "cooldownSec", COOLDOWN.getSeconds());
        } catch (NurigoMessageNotReceivedException e) {
            log.warn("Solapi send failed: {}", e.getMessage());
            // 실패 시, 보관한 코드/쿨다운 정리(선택)
            redis.delete(keyCode(phone));
            redis.delete(cdKey);
            return ResultData.from("F-SEND", "문자 발송에 실패했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("Solapi error", e);
            redis.delete(keyCode(phone));
            redis.delete(cdKey);
            return ResultData.from("F-ERROR", "시스템 오류가 발생했습니다.");
        }
    }

    /** 인증코드 확인 */
    public ResultData<?> confirmCode(String rawPhone, String inputCode) {
        final String phone = normalize(rawPhone);
        final String code = (inputCode == null) ? "" : inputCode.trim();

        if (phone.isBlank()) return ResultData.from("F-PHONE", "전화번호를 입력하세요.");
        if (code.isBlank())  return ResultData.from("F-CODE", "코드를 입력해 주세요.");

        String savedHash = redis.opsForValue().get(keyCode(phone));
        if (savedHash == null) {
            return ResultData.from("F-NOTFOUND", "인증번호가 만료되었습니다. 다시 요청해 주세요.");
        }

        String calcHash = sha256(code + "|" + phone);
        if (!savedHash.equals(calcHash)) {
            return ResultData.from("F-MISMATCH", "인증번호가 일치하지 않습니다.");
        }

        // 성공: 일회용 코드 소멸
        redis.delete(keyCode(phone));
        return ResultData.from("S-OK", "전화번호 인증이 완료되었습니다.", "phone", phone);
    }
}
