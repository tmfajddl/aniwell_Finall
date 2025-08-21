package com.example.RSW.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SmsQuotaService {

    private final RedisTemplate<String, String> redis;

    private static final int DAILY_LIMIT = 10;

    private String todayKst() {
        ZonedDateTime nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return nowKst.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private long secondsUntilMidnightKst() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        ZonedDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay(ZoneId.of("Asia/Seoul"));
        return Duration.between(now, midnight).getSeconds();
    }

    private String globalKey() {
        return "sms:quota:date:" + todayKst() + ":global";
    }

    /** 전역 하루 10건 제한: 1건 소비 시도 (허용되면 true) */
    public synchronized boolean tryConsumeOne() {
        String key = globalKey();
        Long used = redis.opsForValue().increment(key);
        if (used != null && used == 1L) {
            redis.expire(key, secondsUntilMidnightKst(), TimeUnit.SECONDS);
        }
        return used != null && used <= DAILY_LIMIT;
    }

    public long usedToday() {
        String v = redis.opsForValue().get(globalKey());
        return (v == null) ? 0L : Long.parseLong(v);
    }

    public long remainingToday() {
        long used = usedToday();
        long remain = DAILY_LIMIT - used;
        return Math.max(0, remain);
    }
}
