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
import java.util.Map;

@Service
public class PetService {

	@Autowired
	private PetRepository petRepository;

	// 주석: PetService의 필드 주입 추가
	@Autowired
	private PetFoodService petFoodService;

	// 멤버 ID로 펫 목록 호출
	public List<Pet> getPetsByMemberId(int memberId) {
		return petRepository.getPetsByMemberId(memberId);
	}

	// 펫 수정(사진 있음)
	public ResultData updatePet(int petId, String name, String species, String breed, String gender, String birthDate,
			double weight, String photo) {

		// VO 생성 → VO 기반 update 사용
		Pet p = new Pet();
		p.setId(petId);
		p.setName(name);
		p.setSpecies(species);
		p.setBreed(breed);
		p.setGender(gender);
		p.setBirthDate(Date.valueOf(birthDate));
		p.setWeight(weight);
		p.setPhoto(photo);

		// XML의 <update id="updatePet" parameterType="com.example.RSW.vo.Pet">와 매칭
		petRepository.updatePet(p);

		return ResultData.from("S-1", "애완동물 정보 수정 완료");
	}

	// 펫 삭제
	public ResultData deletePet(int id) {
		petRepository.deletePet(id);
		return ResultData.from("S-1", "애완동물 삭제 완료");
	}

	// 펫 등록
	public ResultData insertPet(int memberId, String name, String species, String breed, String gender,
			String birthDate, double weight, String photo) {

		// VO를 만들어 VO 기반 mapper로 호출
		Pet pet = new Pet();
		pet.setMemberId(memberId);
		pet.setName(name);
		pet.setSpecies(species);
		pet.setBreed(breed);
		pet.setGender(gender);
		pet.setBirthDate(Date.valueOf(birthDate));
		pet.setWeight(weight);
		pet.setPhoto(photo);

		petRepository.insertPet(pet);

		// 방금 등록된 pet의 id 가져오기
		int id = petRepository.getLastInsertId();

		return ResultData.from("S-1", "반려동물 등록 성공", "등록 성공 id", id);
	}

	// ✅ [추가] 가장 마지막으로 INSERT 된 PK 조회
	public Integer getLastInsertId() {
		return petRepository.getLastInsertId();
	}

	// 펫 사진 없이 수정
	public ResultData updatePetyWithoutPhoto(int petId, String name, String species, String breed, String gender,
			String birthDate, double weight) {
		petRepository.updatePetWithoutPhoto(petId, name, species, breed, gender, birthDate, weight);
		return ResultData.from("S-1", "애완동물 정보 수정 완료");
	}

	// ID로 펫 가져오기
	public Pet getPetsById(int petId) {
		return petRepository.getPetsById(petId);
	}

	// PetService.java (시그니처 예시)
	// ※ 내부에서는 Repository/MyBatis로 아래 동작 수행:
	// 1) 최신 몸무게 조회
	// 2) 변화 판단 (abs(new - last) >= 0.05)
	// 3) 변화 시: pet_weight_log INSERT (petId, measuredAt=NOW, weightKg, source,
	// note)
	// 4) (선택) pet.weightKg / weightUpdatedAt UPDATE
	// (기존 선언부 교체) upsertWeightIfChanged 선언 → 구현으로 교체
	// ✅ [구현] 최신 체중과 비교하여 변화(≥ 0.05kg) 시 로그 적재 + 현재값 업데이트
	public void upsertWeightIfChanged(int petId, double newWeightKg, String source, String note) {
		// 1) 최신 몸무게 조회(없으면 null)
		Double lastWeight = petRepository.findLastWeightByPetId(petId);

		// 2) 변화 판단(임계값 이상일 때만 INSERT/UPDATE)
		if (lastWeight == null || Double.compare(newWeightKg, lastWeight) != 0) {
			// 3) 로그 INSERT (pet_weight_log)
			petRepository.insertWeightLog(petId, newWeightKg, source, note);
			// 4) (선택) pet 테이블 현재 체중/갱신시각 업데이트(컬럼 보유 시)
			petRepository.updatePetWeight(petId, newWeightKg);
		}
	}

	// ✅ [구현] 최초 등록 시 초기 로그 1건 적재 + 현재값 업데이트
	public void insertInitialWeight(int petId, double weightKg, String source, String note) {
		petRepository.insertWeightLog(petId, weightKg, source, note);
		petRepository.updatePetWeight(petId, weightKg);
	}

	// ✅ [구현] 보조 조회: 방금 등록된 petId 확보가 어려울 때 사용
	public Integer findNewestPetIdByMemberAndName(int memberId, String name) {
		return petRepository.findNewestPetIdByMemberAndName(memberId, name);
	}

	// ✅ 값 동일 여부와 무관하게 '항상' 몸무게 로그를 남김
	public void insertWeightAlways(int petId, double weightKg, String source, String note) {
		// 1) 측정 히스토리 보존: 로그 테이블에 무조건 INSERT
		petRepository.insertWeightLog(petId, weightKg, source, note); // <-- 기존 매퍼 재사용

		// 2) 펫 현재 체중은 최신값으로 갱신(같은 값이어도 그대로 덮어쓰기)
		petRepository.updatePetWeight(petId, weightKg); // <-- 기존 매퍼 재사용
	}

	// ✅ 사료량 변화 시에만 로그 적재 (임계값 없음: 값이 다르면 기록)
	// - foodName : 제품명(없으면 null 허용)
	// - feedType : 'dry' | 'wet' (없으면 null 허용)
	// - brand : 브랜드(없으면 null 허용)
	// ✅ 진행중 기본사료와 다르면: 기존 endedAt=오늘, 새 레코드 startedAt=오늘 생성
	@Transactional // ★ 추가: 기본사료 종료/시작이 하나의 트랜잭션으로 커밋되도록
	public void upsertPrimaryFoodIfChanged(int petId, String brand, String feedType) {
		// ★ 방어코드: null/공백은 DB 제약/비교에서 문제를 일으킴
		if (brand == null || brand.isBlank() || feedType == null || feedType.isBlank())
			return;

		var cur = petRepository.findActivePrimaryFood(petId); // {brand, feedType}를 반환하도록 매퍼 정합
		String curBrand = (cur == null) ? null : (String) cur.get("brand");
		String curType = (cur == null) ? null : (String) cur.get("feedType"); // ★ 키명 'feedType'로 통일

		// 동일하면 아무 것도 안 함
		if (cur != null && brand.equalsIgnoreCase(curBrand) && feedType.equalsIgnoreCase(curType))
			return;

		// 기존 활성 종료 후 새 기본사료 시작
		if (cur != null) {
			petRepository.closeActivePrimaryFood(petId); // endedAt = NOW()
		}
		petRepository.insertPrimaryFood(petId, brand, feedType); // startedAt = NOW(), endedAt = NULL
	}

	// ✅ 무게 없이 이벤트 1건 기록 → 일별 COUNT(*)로 "하루 몇 번" 계산
	// ✅ (변경) 급여 이벤트 기록: amountG(0.00) + fedAt=NOW 강제 세팅
	@Transactional // ★ 추가: 이벤트 기록이 롤백되지 않도록
	public void insertFeedEvent(int petId, String feedType, String brand) {
		if (brand == null || brand.isBlank() || feedType == null || feedType.isBlank())
			return;

		// ★ amountG는 NOT NULL 스키마 대비 안전 기본값 사용
		double amountG = 0.00;
		String source = "manual";
		String note = "수정화면 자동기록";

		// ★ fedAt=NOW()는 SQL에서 넣도록 하고, 여기서는 파라미터 최소화
		petRepository.insertFeedEvent(petId, amountG, null, feedType, brand, source, note);
	}

	@Transactional
	public void insertPet(Pet pet, String brand, String feedType) {
		petRepository.insertPet(pet);
		int petId = petRepository.getLastInsertId();

		// ★ feed log 자동 기록도 amountG 기본값으로 안전 삽입
		PetFeedLog log = new PetFeedLog();
		log.setPetId(petId);
		log.setBrand(brand);
		log.setFeedType(feedType);
		log.setSource("manual");
		log.setAmountG(0.00); // ★ 추가: NOT NULL 대비
		log.setFoodName(null); // 선택
		petRepository.insertFeedLog(log); // 매퍼에서 fedAt=NOW(), reg/updateDate=NOW()
	}

	@Transactional
	public void updatePet(Pet pet, String brand, String feedType) {
		petRepository.updatePet(pet);

		PetFeedLog log = new PetFeedLog();
		log.setPetId(pet.getId());
		log.setBrand(brand);
		log.setFeedType(feedType);
		log.setSource("manual");
		log.setAmountG(0.00); // ★ 추가
		log.setFoodName(null);
		petRepository.insertFeedLog(log); // 매퍼에서 시간을 NOW()로 채움
	}

	/**
	 * ✅ 펫에게 급여 기록을 남기는 메서드 - brand/productName이 존재하면 pet_food 테이블에 upsert 후 foodId
	 * 획득 - 이후 pet_feed_log 테이블에 feed 이벤트 기록
	 */
	public void feedPet(int petId, String brand, String productName, String flavor, String feedType) {
		Integer foodId = null;

		// 🔸 브랜드와 제품명이 비어있지 않을 경우 → pet_food 테이블에 upsert & foodId 반환
		if (!Ut.isEmptyOrNull(brand) && !Ut.isEmptyOrNull(productName)) {
			foodId = petFoodService.upsertAndGetId(petId, brand.trim(), productName.trim(), flavor, feedType);
		}

		// 🔸 최종 단계: pet_feed_log 테이블에 급여 이벤트 기록
		insertFeedEvent(petId, foodId, feedType, brand);
	}

	/**
	 * ✅ 실제 DB에 급여 이벤트 기록 (pet_feed_log INSERT)
	 * 
	 * @param petId    펫 ID
	 * @param foodId   pet_food 테이블의 ID (없으면 NULL)
	 * @param feedType 급여 형태(dry/wet/treat)
	 * @param brand    브랜드명
	 */
	public void insertFeedEvent(int petId, Integer foodId, String feedType, String brand) {
		petRepository.insertFeedEventSimple(petId, foodId, feedType, brand);
	}

	public List<Map<String, Object>> getPetsWithFood(int memberId) {
		return petRepository.getPetsByMemberIdWithLatestFood(memberId);
	}
}
