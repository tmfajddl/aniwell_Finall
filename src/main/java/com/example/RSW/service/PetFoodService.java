package com.example.RSW.service;

import com.example.RSW.repository.PetRepository;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Pet;
import com.example.RSW.vo.PetFeedLog;
import com.example.RSW.vo.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

// 주석: 사료/간식 메타데이터(pet_food) 전담 서비스

@Service
public class PetFoodService {

	// 주석: 전용 리포지터리가 있으면 더 좋습니다. (예: PetFoodRepository)
	@Autowired
	private PetRepository petRepository;

	// 주석: (brand, productName, flavor, feedType)를 기준으로 upsert 후 PK 반환
	public Integer upsertAndGetId(int petId, String brand, String productName, String flavor, String feedType) {
		// 1) 필수값 트림/검증
		if (brand == null || brand.isBlank() || productName == null || productName.isBlank())
			return null;

		// 2) 업서트 수행 (MyBatis: ON DUPLICATE KEY UPDATE 혹은 MERGE 등)
		// 아래는 예시. 실제로는 Mapper XML에 구현하고 호출합니다.
		petRepository.upsertPetFood(brand.trim(), productName.trim(), flavor, feedType);

		// 3) 방금(혹은 기존) 레코드의 id를 반환
		return petRepository.findPetFoodId(brand.trim(), productName.trim(), flavor, feedType);
	}
}
