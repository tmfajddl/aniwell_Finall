package com.example.RSW.api;

import com.example.RSW.service.SmsQuotaService;
import com.example.RSW.vo.ResultData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class ApiSmsController {

    private final SmsQuotaService smsQuotaService;

    /** 프론트에서 실제 SMS 전송 전에 이 엔드포인트를 먼저 호출해서 카운트 소비 */
    @PostMapping("/consume")
    public ResultData<?> consume() {
        boolean allowed = smsQuotaService.tryConsumeOne();
        if (!allowed) {
            // ResultData 포맷은 프로젝트에 맞춰 사용 (S-/F- 코드 사용)
            return ResultData.from("F-LIMIT", "오늘 무료 10건을 모두 사용했습니다.",
                    "used", smsQuotaService.usedToday(),
                    "remaining", 0);
        }
        return ResultData.from("S-OK", "허용",
                "used", smsQuotaService.usedToday(),
                "remaining", smsQuotaService.remainingToday());
    }

    /** 남은 건수 조회 (옵션) */
    @GetMapping("/remaining")
    public ResultData<?> remaining() {
        return ResultData.from("S-OK", "잔여 건수",
                "used", smsQuotaService.usedToday(),
                "remaining", smsQuotaService.remainingToday());
    }
}
