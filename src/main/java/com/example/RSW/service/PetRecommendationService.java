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

    public List<String> getFavoriteNamesByMember(int memberId) {
        return recommendationRepository.selectPlaceNamesByMemberId(memberId);
    }

    public boolean isFavorited(int memberId, String name) {
        return recommendationRepository.countByMemberAndName(memberId, name) > 0;
    }

    public void saveFavorite(int memberId, String type, String name, String address, String phone, String mapUrl) {
        recommendationRepository.insert(memberId, type, name, address, phone, mapUrl);
    }

    public void removeFavorite(int memberId, String name) {
        recommendationRepository.deleteByMemberAndName(memberId, name);
    }
}
