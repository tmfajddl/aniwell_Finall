package com.example.RSW.service;

import com.example.RSW.dto.PetHealthLogDto;
import com.example.RSW.repository.PetHealthRepository;
import com.example.RSW.vo.PetHealthLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PetHealthService {

    @Autowired
    private PetHealthRepository repo;

    public void save(PetHealthLogDto dto) {
        Map<String, Object> param = new HashMap<>();
        param.put("petId", dto.getPetId());
        param.put("logDate", dto.getLogDate());
        param.put("foodWeight", dto.getFoodWeight());
        param.put("waterWeight", dto.getWaterWeight());
        param.put("litterCount", dto.getLitterCount());
        param.put("soundLevel", dto.getSoundLevel());
        param.put("notes", dto.getNotes());
        repo.insertLog(param);
    }

    public List<PetHealthLog> getLogsByPetId(int petId) {
        return repo.findLogsByPetId(petId);
    }
}

