package com.example.RSW.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Pet {
	private int id;
	private int memberId;
	private String name;
	private String species;
	private String breed;
	private String gender;
	private Date birthDate;
	private double weight;
	private String photo;
	private Timestamp createdAt;
	private Timestamp updatedAt;

	// 🔹 JOIN으로 가져온 사료 정보
	private String brand; // 사료 브랜드
	private String productName; // 사료 제품명
	private String flavor; // 사료 맛/주요 성분
	private String foodType; // 사료 형태(dry, wet, treat 등)

}
