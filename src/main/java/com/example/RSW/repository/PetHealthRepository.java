package com.example.RSW.repository;

import com.example.RSW.vo.PetHealthLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface PetHealthRepository {
    void insertLog(PetHealthLog log);  // Map 대신 VO 사용
    List<PetHealthLog> findLogsByPetId(int petId);
}

