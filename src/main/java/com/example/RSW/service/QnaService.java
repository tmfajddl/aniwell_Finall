package com.example.RSW.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.RSW.repository.QnaRepository;
import com.example.RSW.vo.Qna;

import java.util.List;

@Service
public class QnaService {

    @Autowired
    private QnaRepository qnaRepository;

    public List<Qna> getPublicFaqList() {
        return qnaRepository.getFaqList();
    }

    public Qna getQnaById(int id) {
        return qnaRepository.getQnaById(id);
    }

    public void writeUserQna(int loginedMemberId, String title, String body, boolean isSecret) {
        Qna qna = new Qna();
        qna.setMemberId(loginedMemberId);
        qna.setTitle(title);
        qna.setBody(body);
        qna.setSecret(isSecret);
        qna.setFromUser(true);
        qna.setAnswered(false);
        qna.setActive(true);
        qnaRepository.saveUserQna(qna);
    }

    public List<Qna> getUserQnaByMemberId(int memberId) {
        return qnaRepository.getUserQnaByMemberId(memberId);
    }

    public void deleteQna(int id) {
        qnaRepository.markQnaAsDeleted(id);
    }

    public void modifyQna(int id, String title, String body, boolean isSecret) {
        qnaRepository.updateQna(id, title, body, isSecret);
    }
}