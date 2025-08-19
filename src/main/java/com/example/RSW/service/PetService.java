package com.example.RSW.service;

import com.example.RSW.repository.PetRepository;
import com.example.RSW.vo.Pet;
import com.example.RSW.vo.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetService {

	@Autowired
	private PetRepository petRepository;

	// 멤버 ID로 펫 목록 호출
	public List<Pet> getPetsByMemberId(int memberId) {
		return petRepository.getPetsByMemberId(memberId);
	}

	// 펫 수정(사진 있음)
	public ResultData updatePet(int petId, String name, String species, String breed, String gender, String birthDate,
			double weight, String photo) {
		petRepository.updatePet(petId, name, species, breed, gender, birthDate, weight, photo);
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

		petRepository.insertPet(memberId, name, species, breed, gender, birthDate, weight, photo);

		// 방금 등록된 pet의 id 가져오기
		int id = petRepository.getLastInsertId();

		return ResultData.from("S-1", "반려동물 등록 성공", "등록 성공 id", id);
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
}
