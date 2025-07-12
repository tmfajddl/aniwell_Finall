package com.example.RSW.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrewMember {
    private int id;             // 고유 ID (AUTO_INCREMENT)
    private int crewId;         // 크루 ID
    private int memberId;       // 회원 ID
    private LocalDateTime joinedAt;  // 가입 일시

}
