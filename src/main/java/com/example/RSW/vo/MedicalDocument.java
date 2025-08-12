package com.example.RSW.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MedicalDocument {
    private int id;
    private int visitId;
    private String docType;   // 'receipt','prescription','lab','diagnosis'
    private String fileUrl;
    private String ocrJson;   // JSON 문자열 그대로 저장/로드
    private LocalDateTime createdAt;
}
