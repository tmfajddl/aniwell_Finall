package com.example.RSW.service;

import com.example.RSW.repository.PetRecommendationRepository;
import com.example.RSW.vo.PetRecommendation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetRecommendationService {

    @Autowired
    private PetRecommendationRepository recommendationRepository;

    public boolean saveIfNotExists(PetRecommendation rec) {
        PetRecommendation existing = recommendationRepository.findByMemberAndName(rec.getMemberId(), rec.getName());
        if (existing != null) return false;

        recommendationRepository.insert(rec);
        return true;
    }

    public List<PetRecommendation> getFavorites(int memberId) {
        return recommendationRepository.findByMember(memberId);
    }

    public void deleteById(int id) {
        recommendationRepository.deleteById(id);
    }

    public PetRecommendation findByMemberAndName(int memberId, String name) {

        return recommendationRepository.findByMemberAndName(memberId, name);
    }
}
