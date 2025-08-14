package com.example.RSW.service;

import com.example.RSW.repository.MedicalDocumentRepository;
import com.example.RSW.vo.MedicalDocument;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicalDocumentService {

    @Autowired
    private MedicalDocumentRepository medicalDocumentRepository;

    public int insertDocument(MedicalDocument doc){
        return medicalDocumentRepository.insertDocument(doc);
    }

    public int updateDocument(MedicalDocument doc){
        return medicalDocumentRepository.updateDocument(doc);
    }

    public void deleteDocument(@Param("id") int id){
        medicalDocumentRepository.deleteDocument(id);
    }

    public MedicalDocument selectById(@Param("id") int id){
        return medicalDocumentRepository.selectById(id);
    }

    public List<MedicalDocument> selectByVisitId(@Param("visitId") int visitId){
        return medicalDocumentRepository.selectByVisitId(visitId);
    }

    // visit JOIN 해서 petId로 조회
    public List<MedicalDocument> selectByPetId(@Param("petId") int petId){
        return medicalDocumentRepository.selectByPetId(petId);
    }

}
