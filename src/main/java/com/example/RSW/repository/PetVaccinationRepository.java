package com.example.RSW.repository;

import com.example.RSW.vo.PetVaccination;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PetVaccinationRepository {
    List<PetVaccination> getVaccinationByPetId(int petId);

}
