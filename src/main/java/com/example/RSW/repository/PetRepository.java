package com.example.RSW.repository;

import com.example.RSW.vo.Pet;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PetRepository {
    List<Pet> getPetsByMemberId(int memberId);
}
