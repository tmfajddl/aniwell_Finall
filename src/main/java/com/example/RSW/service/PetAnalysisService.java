package com.example.RSW.service;

import com.example.RSW.repository.PetAnalysisRepository;
import com.example.RSW.vo.PetAnalysis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetAnalysisService {

    @Autowired
    private PetAnalysisRepository petAnalysisRepository;

    public List<PetAnalysis> getAnalysisByPetId(int petId) {
        return petAnalysisRepository.getAnalysisByPetId(petId);
    }
}
