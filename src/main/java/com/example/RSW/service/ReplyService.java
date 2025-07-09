package com.example.RSW.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.RSW.repository.ReplyRepository;
import com.example.RSW.vo.Reply;

@Service
public class ReplyService {

    @Autowired
    private ReplyRepository replyRepository;

    public ReplyService(ReplyRepository replyRepository) {
        this.replyRepository = replyRepository;
    }

    public List<Reply> getForPrintReplies(String relTypeCode, int id) {
        return replyRepository.getForPrintReplies(relTypeCode, id);
    }

}