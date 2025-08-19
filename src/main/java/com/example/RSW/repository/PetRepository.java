package com.example.RSW.repository;

import com.example.RSW.vo.Pet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PetRepository {
	List<Pet> getPetsByMemberId(int memberId); // 회원 ID로 펫 목록 조회

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

	// ✅ [추가] 가장 최근 체중 1건(없으면 null)
	Double findLastWeightByPetId(@Param("petId") int petId);

	// ✅ [추가] 체중 로그 INSERT (pet_weight_log)
	int insertWeightLog(@Param("petId") int petId, @Param("weightKg") double weightKg, @Param("source") String source,
			@Param("note") String note);

	// ✅ [추가] 펫 현재 체중 업데이트 (weightUpdatedAt 컬럼이 없다면 XML에서 해당 컬럼 줄은 주석 처리)
	int updatePetWeight(@Param("petId") int petId, @Param("weightKg") double weightKg);

	// ✅ [추가] 방금 등록한 펫(동명이인 대비: memberId+name) 중 가장 최근 PK
	Integer findNewestPetIdByMemberAndName(@Param("memberId") int memberId, @Param("name") String name);

	// ✅ [추가] 최신 사료 급여량 조회
	// - pet_feed_log 테이블에서 petId 기준 최신 amount_g 값 조회
	Double findLastFeedAmountByPetId(@Param("petId") int petId);

	// ✅ [추가] 사료 로그 적재
	// - 급여량 변화가 있을 때만 INSERT
	int insertFeedLog(@Param("petId") int petId, @Param("amountG") double amountG, @Param("foodName") String foodName,
			@Param("feedType") String feedType, @Param("brand") String brand, @Param("source") String source,
			@Param("note") String note);

	// ✅ [추가] 펫 테이블의 최신 사료 정보 업데이트 (선택적으로 사용)
	int updatePetFeed(@Param("petId") int petId, @Param("amountG") double amountG, @Param("foodName") String foodName,
			@Param("feedType") String feedType, @Param("brand") String brand);

}
