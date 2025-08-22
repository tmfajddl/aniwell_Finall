package com.example.RSW.api;

import com.example.RSW.service.EmailVerificationService;
import com.example.RSW.vo.ResultData;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/verify/email")
@RequiredArgsConstructor
@Validated
public class ApiEmailVerifyController {

    private final EmailVerificationService emailVerificationService;

    // 인증코드 전송
    @PostMapping("/send")
    public ResultData<?> send(@RequestBody SendReq req) {
        return emailVerificationService.sendCode(req.getEmail(), req.getPurpose());
    }

    // 인증코드 확인
    @PostMapping("/check")
    public ResultData<?> check(@RequestBody CheckReq req, HttpSession session) {
        return emailVerificationService.verifyCode(req.getTxId(), req.getCode(), req.getPurpose(), session);
    }

    // === DTOs ===
    @Data
    public static class SendReq {
        @Email @NotBlank private String email;
        /** 예: "join" (목적 구분 – 선택값) */
        private String purpose = "join";
    }

    @Data
    public static class CheckReq {
        @NotBlank private String txId;
        @NotBlank private String code;
        private String purpose = "join";
    }
}
