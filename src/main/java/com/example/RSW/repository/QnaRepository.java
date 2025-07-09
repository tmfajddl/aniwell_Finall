package com.example.RSW.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.RSW.vo.Qna;

import java.util.List;

@Mapper
public interface QnaRepository {

    List<Qna> getFaqList();

    Qna getQnaById(int id);

    void saveUserQna(Qna qna);

    List<Qna> getUserQnaByMemberId(int memberId);
}