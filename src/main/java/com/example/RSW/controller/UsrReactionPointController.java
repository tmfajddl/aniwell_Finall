package com.example.RSW.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.RSW.service.ArticleService;
import com.example.RSW.service.BoardService;
import com.example.RSW.service.ReactionPointService;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Article;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Rq;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UsrReactionPointController {

    @Autowired
    private Rq rq;

    @Autowired
    private ReactionPointService reactionPointService;

    @Autowired
    private ArticleService articleService;

    // 👍 좋아요 처리
    @RequestMapping("/usr/reactionPoint/doGoodReaction")
    @ResponseBody
    public ResultData doGoodReaction(String relTypeCode, int relId, String replaceUri) {

        // 현재 사용자의 반응 상태 확인 (1: 좋아요, -1: 싫어요, 0: 없음)
        ResultData usersReactionRd = reactionPointService.usersReaction(rq.getLoginedMemberId(), relTypeCode, relId);
        int usersReaction = (int) usersReactionRd.getData1();

        if (usersReaction == 1) {
            // 이미 좋아요 눌렀을 경우 → 좋아요 취소
            reactionPointService.deleteGoodReactionPoint(rq.getLoginedMemberId(), relTypeCode, relId);
            return ResultData.from("S-1", "좋아요 취소",
                    "goodRP", articleService.getGoodRP(relId),
                    "badRP", articleService.getBadRP(relId));
        } else if (usersReaction == -1) {
            // 이전에 싫어요 → 싫어요 취소 + 좋아요 처리
            reactionPointService.deleteBadReactionPoint(rq.getLoginedMemberId(), relTypeCode, relId);
            reactionPointService.addGoodReactionPoint(rq.getLoginedMemberId(), relTypeCode, relId);
            return ResultData.from("S-2", "싫어요 했었음",
                    "goodRP", articleService.getGoodRP(relId),
                    "badRP", articleService.getBadRP(relId));
        }

        // 처음 좋아요 처리
        ResultData reactionRd = reactionPointService.addGoodReactionPoint(rq.getLoginedMemberId(), relTypeCode, relId);
        if (reactionRd.isFail()) {
            return ResultData.from(reactionRd.getResultCode(), reactionRd.getMsg());
        }

        return ResultData.from(reactionRd.getResultCode(), reactionRd.getMsg(),
                "goodRP", articleService.getGoodRP(relId),
                "badRP", articleService.getBadRP(relId));
    }

    // 👎 싫어요 처리
    @RequestMapping("/usr/reactionPoint/doBadReaction")
    @ResponseBody
    public ResultData doBadReaction(String relTypeCode, int relId, String replaceUri) {

        // 현재 반응 상태 확인
        ResultData usersReactionRd = reactionPointService.usersReaction(rq.getLoginedMemberId(), relTypeCode, relId);
        int usersReaction = (int) usersReactionRd.getData1();

        if (usersReaction == -1) {
            // 이미 싫어요 눌렀을 경우 → 싫어요 취소
            reactionPointService.deleteBadReactionPoint(rq.getLoginedMemberId(), relTypeCode, relId);
            return ResultData.from("S-1", "싫어요 취소",
                    "goodRP", articleService.getGoodRP(relId),
                    "badRP", articleService.getBadRP(relId));
        } else if (usersReaction == 1) {
            // 이전에 좋아요 → 좋아요 취소 + 싫어요 처리
            reactionPointService.deleteGoodReactionPoint(rq.getLoginedMemberId(), relTypeCode, relId);
            reactionPointService.addBadReactionPoint(rq.getLoginedMemberId(), relTypeCode, relId);
            return ResultData.from("S-2", "좋아요 했었음",
                    "goodRP", articleService.getGoodRP(relId),
                    "badRP", articleService.getBadRP(relId));
        }

        // 처음 싫어요 처리
        ResultData reactionRd = reactionPointService.addBadReactionPoint(rq.getLoginedMemberId(), relTypeCode, relId);
        if (reactionRd.isFail()) {
            return ResultData.from(reactionRd.getResultCode(), reactionRd.getMsg());
        }

        return ResultData.from(reactionRd.getResultCode(), reactionRd.getMsg(),
                "goodRP", articleService.getGoodRP(relId),
                "badRP", articleService.getBadRP(relId));
    }

    // 토글 방식 리액션 (Ajax 용: 좋아요/싫어요 토글)
    @PostMapping("/toggle")
    @ResponseBody
    public ResultData<?> toggleReaction(HttpServletRequest req, String relTypeCode, int relId) {
        Rq rq = (Rq) req.getAttribute("rq");
        return reactionPointService.toggleReaction(rq.getLoginedMemberId(), relTypeCode, relId);
    }
}
