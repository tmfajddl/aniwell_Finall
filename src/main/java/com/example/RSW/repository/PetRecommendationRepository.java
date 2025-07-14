package com.example.RSW.repository;

import com.example.RSW.vo.PetRecommendation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PetRecommendationRepository {

    PetRecommendation findByMemberAndName(int memberId, String name);

    void insert(PetRecommendation rec);

    List<PetRecommendation> findByMember(int memberId);

    void deleteById(int id);
}
