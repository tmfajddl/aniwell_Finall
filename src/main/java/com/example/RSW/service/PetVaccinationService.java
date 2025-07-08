package com.example.RSW.service;

import com.example.RSW.repository.PetVaccinationRepository;
import com.example.RSW.vo.PetVaccination;
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

}
