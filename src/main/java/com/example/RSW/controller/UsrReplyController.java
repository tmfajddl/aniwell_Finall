package com.example.RSW.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.RSW.service.ReactionPointService;
import com.example.RSW.service.ReplyService;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Reply;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Rq;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@Controller
public class UsrReplyController {

    @Autowired
    private Rq rq;

    @Autowired
    private ReactionPointService reactionPointService;

    @Autowired
    private ReplyService replyService;

    // 댓글 작성 처리
    @RequestMapping("/usr/reply/doWrite")
    @ResponseBody
    public ResultData<Reply> doWrite(HttpServletRequest req, String relTypeCode, int relId, String body) {
        Rq rq = (Rq) req.getAttribute("rq");

        if (Ut.isEmptyOrNull(body)) {
            return ResultData.from("F-2", "내용을 입력해주세요");
        }

        ResultData writeReplyRd = replyService.writeReply(rq.getLoginedMemberId(), body, relTypeCode, relId);
        int id = (int) writeReplyRd.getData1();
        Reply reply = replyService.getReply(id);

        return ResultData.from("S-1", "댓글이 등록되었습니다", reply);
    }

    @RequestMapping("/usr/reply/list")
    @ResponseBody
    public List<Reply> getReplies(String relTypeCode, int relId) {
        return replyService.getForPrintReplies(rq.getLoginedMemberId(), relTypeCode, relId);
    }

    @RequestMapping("/usr/reply/doModify")
    @ResponseBody
    public ResultData<String> doModify(HttpServletRequest req, int id, String body) {
        Rq rq = (Rq) req.getAttribute("rq");
        Reply reply = replyService.getReply(id);

        if (reply == null) {
            return ResultData.from("F-1", Ut.f("%d번 댓글은 존재하지 않습니다", id));
        }

        ResultData canModify = replyService.userCanModify(rq.getLoginedMemberId(), reply);
        if (canModify.isFail()) return canModify;

        replyService.modifyReply(id, body);
        return ResultData.from("S-1", "댓글이 수정되었습니다");
    }

    @RequestMapping("/usr/reply/doDelete")
    @ResponseBody
    public ResultData<Integer> doDelete(HttpServletRequest req, int id) {
        Rq rq = (Rq) req.getAttribute("rq");
        Reply reply = replyService.getReply(id);

        if (reply == null) return ResultData.from("F-1", "해당 댓글이 존재하지 않습니다.");

        ResultData canDelete = replyService.userCanDelete(rq.getLoginedMemberId(), reply);
        if (canDelete.isFail()) return canDelete;

        replyService.deleteReply(id);
        return ResultData.from("S-1", "댓글이 삭제되었습니다", id);
    }

}
