package com.example.RSW.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

import com.example.RSW.vo.Member;
import com.example.RSW.vo.WalkCrew;

@Mapper
public interface WalkCrewMemberRepository {

	// 크루 참가 요청 (insert)
	void requestToJoinCrew(@Param("crewId") int crewId, @Param("memberId") int memberId);

}