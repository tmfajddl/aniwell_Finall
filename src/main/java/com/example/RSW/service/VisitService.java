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

	// [수정] 생성된 PK를 반환하도록 교정
	public int insertVisit(Visit visit) {
		// 🔸 여기서 MyBatis가 INSERT 수행
		// (VisitRepository.xml의 <insert>에 useGeneratedKeys="true" keyProperty="id" 필수)
		visitRepository.insertVisit(visit);

		// 🔸 위 옵션 덕분에 visit.id(생성된 PK)가 객체에 주입됨
		return visit.getId(); // ✅ 영향 행수(1)가 아닌, '생성된 PK'를 반환
	}

	public int updateVisit(Visit visit) {
		return visitRepository.updateVisit(visit);
	}

	public void deleteVisit(int id) {
		visitRepository.deleteVisit(id);
	}

	public List<Visit> selectVisitsByPetId(int petId) {
		return visitRepository.selectVisitsByPetId(petId);
	}

}
