package com.example.RSW.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.RSW.repository.BoardRepository;
import com.example.RSW.repository.DistrictRepository;
import com.example.RSW.repository.MemberRepository;
import com.example.RSW.repository.WalkCrewMemberRepository;
import com.example.RSW.repository.WalkCrewRepository;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.District;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.WalkCrew;
import com.example.RSW.vo.WalkCrewMember;

@Service
public class WalkCrewMemberService {

	@Autowired
	WalkCrewMemberRepository walkCrewMemberRepository;

	@Autowired
	WalkCrewService walkCrewService;

	// ✅ 크루 참가 요청
	public void requestToJoinCrew(int crewId, int memberId) {
		walkCrewMemberRepository.requestToJoinCrew(crewId, memberId);
	}

	// ✅ 내가 가입한 크루 1개 가져오기 (크루 카페 진입용)
	public WalkCrew getMyCrew(int memberId) {
		return walkCrewMemberRepository.findMyCrewByMemberId(memberId);
	}

	// ✅ 내가 신청한 크루 리스트
	public List<WalkCrew> getCrewsAppliedBy(int memberId) {
		return walkCrewMemberRepository.findCrewsAppliedBy(memberId);
	}

	// ✅ 내가 리더인 크루에 대한 신청자 리스트
	public List<Map<String, Object>> getRequestListForLeader(int leaderId) {
		return walkCrewMemberRepository.findRequestListByLeaderId(leaderId);
	}

	// ✅ 크루에 이미 가입했는지 여부 확인
	public boolean isJoinedCrew(int memberId, int crewId) {
		return walkCrewMemberRepository.countByMemberIdAndCrewId(memberId, crewId) > 0;
	}

	// ✅ 강퇴 & 탈퇴 공통 처리
	public boolean expelMemberFromCrew(int crewId, int memberId) {
		int affectedRows = walkCrewMemberRepository.deleteMemberFromCrew(crewId, memberId);
		return affectedRows > 0;
	}

	// 크루멤버리스트
	public List<WalkCrewMember> getMembersByCrewId(int crewId) {
		return walkCrewMemberRepository.findMembersByCrewId(crewId);
	}

	public boolean transferLeadership(int crewId, int currentLeaderId, int newLeaderId) {
		try {
			// 1. walk_crew 테이블의 leaderId 변경
			walkCrewService.updateLeader(crewId, newLeaderId);

			// 2. 기존 리더 → subleader
			walkCrewMemberRepository.updateRole(currentLeaderId, crewId, "subleader");

			// 3. 새로운 리더 → leader
			walkCrewMemberRepository.updateRole(newLeaderId, crewId, "leader");

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String getRole(int memberId, int crewId) {
		return walkCrewMemberRepository.findRoleByMemberIdAndCrewId(memberId, crewId);
	}

	public boolean isMemberOfCrew(int memberId, int crewId) {
		return walkCrewMemberRepository.countByMemberIdAndCrewId(memberId, crewId) > 0;
	}

}