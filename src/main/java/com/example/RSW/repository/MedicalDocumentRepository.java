package com.example.RSW.repository;

import com.example.RSW.vo.MedicalDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MedicalDocumentRepository {

	int insertDocument(MedicalDocument doc);

	int updateDocument(MedicalDocument doc);

	void deleteDocument(@Param("id") int id);

	MedicalDocument selectById(@Param("id") int id);

	List<MedicalDocument> selectByVisitId(@Param("visitId") int visitId);

	// visit JOIN 해서 petId로 조회
	List<MedicalDocument> selectByPetId(@Param("petId") int petId);

	MedicalDocument findById(int id);

	MedicalDocument findLatestByVisitId(int visitId);
}
