package com.example.RSW.service;

import com.example.RSW.dto.PetHealthLogDto;
import com.example.RSW.repository.PetHealthRepository;
import com.example.RSW.vo.PetHealthLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PetHealthService {

    @Autowired
    private PetHealthRepository repo;

    // ✅ Dto를 VO로 변환해 저장하고, 저장된 VO 반환
    public PetHealthLog save(PetHealthLogDto dto) {
        PetHealthLog log = PetHealthLog.builder()
                .petId(dto.getPetId())
                .logDate(LocalDateTime.parse(dto.getLogDate()))
                .foodWeight(dto.getFoodWeight())
                .waterWeight(dto.getWaterWeight())
                .litterCount(dto.getLitterCount())
                .soundLevel(dto.getSoundLevel())
                .notes(dto.getNotes())
                .build();

        repo.insertLog(log);
        return log;  // WebSocket 브로드캐스트용
    }

    // ✅ VO 자체 저장도 가능하게
    public PetHealthLog save(PetHealthLog log) {
        repo.insertLog(log);
        return log;
    }

    public List<PetHealthLog> getLogsByPetId(int petId) {
        return repo.findLogsByPetId(petId);
    }

    public List<PetHealthLog> getLogsByPetIdAndDate(int petId, LocalDate date) {
        return repo.findByPetIdAndDate(petId, date);
    }
}
