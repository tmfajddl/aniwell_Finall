package com.example.RSW.service;

import com.example.RSW.repository.PetVaccinationRepository;
import com.example.RSW.vo.PetVaccination;
import com.example.RSW.vo.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PetVaccinationService {

    @Autowired
    private final PetVaccinationRepository petVaccinationRepository;

    public PetVaccinationService(PetVaccinationRepository petVaccinationRepository) {
        this.petVaccinationRepository = petVaccinationRepository;
    }

    public List<PetVaccination> getVaccinationsByPetId(int petId) {
        return petVaccinationRepository.getVaccinationByPetId(petId);

    }

    public ResultData deletePetVaccination(int id) {
        petVaccinationRepository.deletePetVaccination(id);
        return ResultData.from("S-1", "접종 정보 삭제 완료");
    }

    public ResultData insertPetVaccination(int petId, String vaccineName, String injectionDate) {
        // 동일 백신의 이전 접종 기록 → nextDueDate NULL 처리
        petVaccinationRepository.invalidateNextDueDates(petId, vaccineName);

        // 새 백신 등록
        petVaccinationRepository.insertVaccination(petId, vaccineName, injectionDate);

        return ResultData.from("S-1", "접종 등록 완료");
    }

    public ResultData updatePetVaccination(int vaccinationId, String vaccineName, String injectionDate) {
        petVaccinationRepository.updatePetVaccination(vaccinationId, vaccineName, injectionDate);
        return ResultData.from("S-1", "접종 정보 수정 완료");
    }

    public PetVaccination getVaccinationsById(int vaccinationId) {
        return petVaccinationRepository.getVaccinationById(vaccinationId);
    }

    public int getPetIdById(int vaccinationId) {
        return petVaccinationRepository.getPetIdById(vaccinationId);
    }

    public ResultData updatePetVaccinationWithNotes(int vaccinationId, String vaccineName, String injectionDate, String notes) {
        petVaccinationRepository.updatePetVaccinationWithNotes(vaccinationId, vaccineName, injectionDate,notes);
        return ResultData.from("S-1", "접종 정보 수정 완료");
    }

    public ResultData insertPetVaccinationWithNotes(int petId, String vaccineName, String injectionDate, String notes) {
        // 동일 백신의 이전 접종 기록 → nextDueDate NULL 처리
        petVaccinationRepository.invalidateNextDueDates(petId, vaccineName);

        // 새 백신 등록
        petVaccinationRepository.insertPetVaccinationWithNotes(petId, vaccineName, injectionDate, notes);

        return ResultData.from("S-1", "접종 등록 완료");
    }
}
