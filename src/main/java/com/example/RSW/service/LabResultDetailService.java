package com.example.RSW.service;

import com.example.RSW.repository.LabResultDetailRepository;
import com.example.RSW.vo.LabResultDetail;
import org.apache.ibatis.annotations.Param;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabResultDetailService {

    @Autowired
    private LabResultDetailRepository labResultDetailRepository;

    public int insert(LabResultDetail row){
        return labResultDetailRepository.insert(row);
    }

    public int[] insertBatch(@Param("list") List<LabResultDetail> list){
        return labResultDetailRepository.insertBatch(list);
    }

    public int update(LabResultDetail row){
        return labResultDetailRepository.update(row);
    }

    public void delete(@Param("id") int id){
        labResultDetailRepository.delete(id);
    }

    public LabResultDetail selectById(@Param("id") int id){
        return labResultDetailRepository.selectById(id);
    }

    public List<LabResultDetail> selectByDocumentId(@Param("documentId") int documentId){
        return labResultDetailRepository.selectByDocumentId(documentId);
    }

    // medical_document JOIN 으로 방문ID 기준
    public List<LabResultDetail> selectByVisitId(@Param("visitId") int visitId){
        return labResultDetailRepository.selectByVisitId(visitId);
    }

    // visit JOIN 으로 펫ID 기준
    public List<LabResultDetail> selectByPetId(@Param("petId") int petId){
        return labResultDetailRepository.selectByPetId(petId);
    }
}
