package com.example.RSW.service;

import java.util.List;

import com.example.RSW.util.Ut;
import com.example.RSW.vo.ResultData;
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

    public List<Reply> getForPrintReplies(int loginedMemberId, String relTypeCode, int id) {
        List<Reply> replies = replyRepository.getForPrintReplies(loginedMemberId, relTypeCode, id);

        for (Reply reply : replies) {
            controlForPrintData(loginedMemberId, reply);
        }

        return replies;
    }

    public ResultData writeReply(int loginedMemberId, String body, String relTypeCode, int relId) {
        replyRepository.writeReply(loginedMemberId, body, relTypeCode, relId);

        int id = replyRepository.getLastInsertId();

        return ResultData.from("S-1", Ut.f("%d번 댓글이 등록되었습니다", id), "등록 된 댓글의 id", id);
    }

    private void controlForPrintData(int loginedMemberId, Reply reply) {
        if (reply == null) {
            return;
        }
        ResultData userCanModifyRd = userCanModify(loginedMemberId, reply);
        reply.setUserCanModify(userCanModifyRd.isSuccess());

        ResultData userCanDeleteRd = userCanDelete(loginedMemberId, reply);
        reply.setUserCanDelete(userCanModifyRd.isSuccess());
    }

    public ResultData userCanDelete(int loginedMemberId, Reply reply) {
        if (reply.getMemberId() != loginedMemberId) {
            return ResultData.from("F-2", Ut.f("%d번 댓글에 대한 삭제 권한이 없습니다", reply.getId()));
        }
        return ResultData.from("S-1", Ut.f("%d번 댓글을 삭제했습니다", reply.getId()));
    }

    public ResultData userCanModify(int loginedMemberId, Reply reply) {
        if (reply.getMemberId() != loginedMemberId) {
            return ResultData.from("F-2", Ut.f("%d번 댓글에 대한 수정 권한이 없습니다", reply.getId()));
        }
        return ResultData.from("S-1", Ut.f("%d번 댓글을 수정했습니다", reply.getId()), "수정된 댓글", reply);
    }

    public Reply getReply(int id) {
        return replyRepository.getReply(id);
    }

    public void modifyReply(int id, String body) {
        replyRepository.modifyReply(id, body);
    }

    public void deleteReply(int id) {
        replyRepository.deleteReply(id);
    }
}