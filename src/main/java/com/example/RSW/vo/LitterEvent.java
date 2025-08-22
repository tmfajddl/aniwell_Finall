package com.example.RSW.vo;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LitterEvent {
    private Long id;
    private Long petId;
    private LocalDateTime detectedAt;
    private String type;           // "pee" | "poop" | "unknown"
    private Double confidence;
    private String visualSignalsJson; // JSON 문자열
    private String anomaliesJson;     // JSON 문자열
    private String notes;
    private String sourceVideo;
    private Long logId;               // 원하면 건강로그 PK
}