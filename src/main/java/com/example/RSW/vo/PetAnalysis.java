package com.example.RSW.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PetAnalysis {
    private int id;
    private int petId;
    private String imagePath;
    private String emotionResult;
    private double confidence;
    private LocalDateTime analyzedAt;
}
