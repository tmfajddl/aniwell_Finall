package com.example.RSW.repository;

import com.example.RSW.vo.PetHealthLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface PetHealthRepository {
    void insertLog(Map<String, Object> param);
    List<PetHealthLog> findLogsByPetId(int petId);
}
