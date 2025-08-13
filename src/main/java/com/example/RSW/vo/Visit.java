package com.example.RSW.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Visit {
    private int id;
    private int petId;
    private LocalDateTime visitDate;
    private String hospital;
    private String doctor;
    private String diagnosis;
    private String notes;
    private BigDecimal totalCost;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
