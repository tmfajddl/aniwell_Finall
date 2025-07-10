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

@Controller
public class UsrReplyController {

    @Autowired
    private Rq rq;

    @Autowired
    private ReactionPointService reactionPointService;

    @Autowired
    private ReplyService replyService;

    @RequestMapping("/usr/reply/doWrite")
    @ResponseBody
    public String doWrite(HttpServletRequest req, String relTypeCode, int relId, String body) {

        Rq rq = (Rq) req.getAttribute("rq");

        if (Ut.isEmptyOrNull(body)) {
            return Ut.jsHistoryBack("F-2", "내용을 입력해주세요");
        }

        ResultData writeReplyRd = replyService.writeReply(rq.getLoginedMemberId(), body, relTypeCode, relId);

        int id = (int) writeReplyRd.getData1();

        return Ut.jsReplace(writeReplyRd.getResultCode(), writeReplyRd.getMsg(), "../article/detail?id=" + relId);
    }

    @RequestMapping("/usr/reply/doModify")
    @ResponseBody
    public String doModify(HttpServletRequest req, int id, String body) {
        System.err.println(id);
        System.err.println(body);
        Rq rq = (Rq) req.getAttribute("rq");

        Reply reply = replyService.getReply(id);

        if (reply == null) {
            return Ut.jsHistoryBack("F-1", Ut.f("%d번 댓글은 존재하지 않습니다", id));
        }

        ResultData loginedMemberCanModifyRd = replyService.userCanModify(rq.getLoginedMemberId(), reply);

        if (loginedMemberCanModifyRd.isSuccess()) {
            replyService.modifyReply(id, body);
        }

        reply = replyService.getReply(id);

        return reply.getBody();
    }

    @RequestMapping("/usr/reply/doDelete")
    @ResponseBody
    public String doDelete(@RequestParam int id,
                           @RequestParam String relTypeCode,
                           @RequestParam int relId,
                           @RequestParam int boardId) {

        Reply reply = replyService.getReply(id);

        if (reply == null) {
            return Ut.jsHistoryBack("F-1", "해당 댓글이 존재하지 않습니다.");
        }

        ResultData actorCanDeleteRd = replyService.userCanDelete(rq.getLoginedMemberId(), reply);

        if (actorCanDeleteRd.isFail()) {
            return Ut.jsHistoryBack(actorCanDeleteRd.getResultCode(), actorCanDeleteRd.getMsg());
        }

        replyService.deleteReply(id);

        return Ut.jsReplace("S-1", "댓글을 삭제했습니다.",
                "../article/detail?id=" + relId + "&boardId=" + boardId);
    }

}