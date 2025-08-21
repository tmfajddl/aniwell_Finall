package com.example.RSW.repository;

import com.example.RSW.vo.Pet;
import com.example.RSW.vo.PetFeedLog;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PetRepository {
	List<Pet> getPetsByMemberId(int memberId); // 회원 ID로 펫 목록 조회

	// VO 기반 insert
	void insertPet(Pet pet);

	// VO 기반 update
	void updatePet(Pet pet);

	void deletePet(int id);

	void insertPet(int memberId, String name, String species, String breed, String gender, String birthDate,
			double weight, String photo);

	int getLastInsertId();

	void updatePetWithoutPhoto(int petId, String name, String species, String breed, String gender, String birthDate,
			double weight);

	void updatePet(int petId, String name, String species, String breed, String gender, String birthDate, double weight,
			String photo);

	Pet getPetsById(int petId);

	List<Pet> findPetsWithBirthdayInDays(List<Integer> integers);

	// [추가] 현재 저장된 펫의 weight 조회
	Double getPetWeightById(@Param("petId") int petId);

	// [추가] 해당 펫의 체중 로그 존재 여부(0이면 로그 없음)
	int countWeightLogsByPetId(@Param("petId") int petId);

	// ✅ [추가] 가장 최근 체중 1건(없으면 null)
	Double findLastWeightByPetId(@Param("petId") int petId);

	// ✅ [추가] 체중 로그 INSERT (pet_weight_log)
	int insertWeightLog(@Param("petId") int petId, @Param("weightKg") double weightKg, @Param("source") String source,
			@Param("note") String note);

	// ✅ [추가] 펫 현재 체중 업데이트 (weightUpdatedAt 컬럼이 없다면 XML에서 해당 컬럼 줄은 주석 처리)
	int updatePetWeight(@Param("petId") int petId, @Param("weightKg") double weightKg);

	// ✅ [추가] 방금 등록한 펫(동명이인 대비: memberId+name) 중 가장 최근 PK
	Integer findNewestPetIdByMemberAndName(@Param("memberId") int memberId, @Param("name") String name);

	// 진행중 기본 사료 1건 조회 (isPrimary=1 AND endedAt IS NULL)
	Map<String, Object> findActivePrimaryFood(@Param("petId") int petId); // {brand, feedType}

	// 진행중 기본 사료 종료 (endedAt = CURRENT_DATE())
	int closeActivePrimaryFood(@Param("petId") int petId);

	// 새 기본 사료 시작 (startedAt = CURRENT_DATE(), endedAt = NULL)
	int insertPrimaryFood(@Param("petId") int petId, @Param("brand") String brand, @Param("feedType") String feedType);

	// 급여 이벤트 기록 (무게 없이 → 브랜드/타입만 기록)
	int insertFeedEvent(@Param("petId") int petId, @Param("amountG") Double amountG, // ★ NOT NULL 대비
			@Param("foodName") String foodName, // nullable
			@Param("feedType") String feedType, @Param("brand") String brand, @Param("source") String source,
			@Param("note") String note);

	// 특정 일자의 급여 횟수 (하루 몇 번 급여했는지)
	int countFeedsOnDate(@Param("petId") int petId, @Param("ymd") String ymd);

	// ✅ [추가] 사료 급여 로그 직접 INSERT (PetFeedLog VO 사용)
	int insertFeedLog(PetFeedLog log);

	// [추가] 업서트로 pet_food 저장 (MySQL의 ON DUPLICATE KEY 활용)
//  - UNIQUE 키: (brand, productName, flavor, feedType) 가정
//  - INSERT 또는 중복이면 updateDate만 갱신하고, LAST_INSERT_ID를 현재 row의 id로 세팅
	void upsertPetFood(@Param("brand") String brand, @Param("productName") String productName,
			@Param("flavor") String flavor, @Param("feedType") String feedType);

	// [선택-보조] 위의 LAST_INSERT_ID 방식이 싫다면 조회로 id 반환하는 메서드도 사용 가능
//  - 테이블에 정확히 하나만 매칭된다는 전제가 있을 때 사용
	Integer findPetFoodId(@Param("brand") String brand, @Param("productName") String productName,
			@Param("flavor") String flavor, @Param("feedType") String feedType);

	// [추가] 급여 이벤트 통합 INSERT (amountG/foodId/foodName 모두 선택적)
//  - amountG가 null이면 0.00으로 보정, fedAt/regDate/updateDate는 NOW()
	void insertFeedEventSimple(@Param("petId") int petId, @Param("foodId") Integer foodId,
			@Param("feedType") String feedType, @Param("brand") String brand);

	List<Map<String, Object>> getPetsByMemberIdWithLatestFood(@Param("memberId") int memberId);
}
