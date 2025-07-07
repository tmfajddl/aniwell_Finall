package com.example.RSW.repository;

import org.apache.ibatis.annotations.Mapper;

import com.example.RSW.vo.Member;

@Mapper
public interface MemberRepository {

	public int doJoin(String loginId, String loginPw, String name, String nickname, String cellphoneNum, String email);

	public Member getMemberById(int id);

	public int getLastInsertId();

	public Member getMemberByLoginId(String loginId);

	public Member getMemberByNameAndEmail(String name, String email);

	public void modify(int loginedMemberId, String loginPw, String name, String nickname, String cellphoneNum,
			String email);

	public void modifyWithoutPw(int loginedMemberId, String name, String nickname, String cellphoneNum, String email);

}