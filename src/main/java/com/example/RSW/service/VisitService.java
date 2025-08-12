package com.example.RSW.service;

import com.example.RSW.repository.VisitRepository;
import com.example.RSW.vo.Visit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisitService {

    @Autowired
    private VisitRepository visitRepository;

    public int insertVisit(Visit visit){
        return visitRepository.insertVisit(visit);
    }

    public int updateVisit(Visit visit){
        return visitRepository.updateVisit(visit);
    }

    public void deleteVisit(int id){
        visitRepository.deleteVisit(id);
    }

    public List<Visit> selectVisitsByPetId(int petId){
        return visitRepository.selectVisitsByPetId(petId);
    }


}
