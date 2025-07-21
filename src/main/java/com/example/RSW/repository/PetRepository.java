package com.example.RSW.repository;

import com.example.RSW.vo.Pet;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PetRepository {
    List<Pet> getPetsByMemberId(int memberId);  // 회원 ID로 펫 목록 조회

    void deletePet(int id);

    void insertPet(int memberId, String name, String species, String breed, String gender, String birthDate, double weight, String photo);

    int getLastInsertId();

    void updatePetWithoutPhoto(int petId, String name, String species, String breed, String gender, String birthDate, double weight);

    void updatePet(int petId, String name, String species, String breed, String gender, String birthDate, double weight, String photo);

    Pet getPetsById(int petId);
}
