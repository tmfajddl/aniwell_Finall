package com.example.RSW.service;

import com.example.RSW.repository.VetAnswerRepository;
import com.example.RSW.vo.VetAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VetAnswerService {

    @Autowired
    private VetAnswerRepository vetAnswerRepository;

    public void write(int qnaId, int memberId, String vetName, String answer) {
        vetAnswerRepository.write(qnaId, memberId, vetName, answer);
    }

    public List<VetAnswer> getByQnaId(int qnaId) {
        return vetAnswerRepository.getByQnaId(qnaId);
    }

    public VetAnswer getById(int id) {
        return vetAnswerRepository.getById(id);
    }

    public void modify(int id, String answer) {
        vetAnswerRepository.update(id, answer);
    }

    public void delete(int id) {
        vetAnswerRepository.delete(id);
    }
}

