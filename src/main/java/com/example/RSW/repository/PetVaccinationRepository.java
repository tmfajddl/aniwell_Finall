package com.example.RSW.repository;

import com.example.RSW.vo.PetVaccination;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PetVaccinationRepository {
    List<PetVaccination> getVaccinationByPetId(int petId);

    // 백신 삭제
    void deletePetVaccination(int id);

    void insertVaccination(int petId, String vaccineName, String injectionDate);

    void updatePetVaccination(int vaccinationId, String vaccineName, String injectionDate);

    PetVaccination getVaccinationById(int vaccinationId);

    int getPetIdById(int vaccinationId);

    void updatePetVaccinationWithNotes(int vaccinationId, String vaccineName, String injectionDate, String notes);

    void insertPetVaccinationWithNotes(int petId, String vaccineName, String injectionDate, String notes);

    void invalidateNextDueDates(int petId, String vaccineName);
}
