package com.example.RSW.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @RequestMapping("/usr/reactionPoint/doGoodReaction")
    @ResponseBody
    public ResultData doGoodReaction(String relTypeCode, int relId, String replaceUri) {

        ResultData usersReactionRd = reactionPointService.usersReaction(rq.getLoginedMemberId(), relTypeCode, relId);

        int usersReaction = (int) usersReactionRd.getData1();

        if (usersReaction == 1) {
            ResultData rd = reactionPointService.deleteGoodReactionPoint(rq.getLoginedMemberId(), relTypeCode, relId);
            int goodRP = articleService.getGoodRP(relId);
            int badRP = articleService.getBadRP(relId);
            return ResultData.from("S-1", "좋아요 취소", "goodRP", goodRP, "badRP", badRP);
        } else if (usersReaction == -1) {
            ResultData rd = reactionPointService.deleteBadReactionPoint(rq.getLoginedMemberId(), relTypeCode, relId);
            rd = reactionPointService.addGoodReactionPoint(rq.getLoginedMemberId(), relTypeCode, relId);
            int goodRP = articleService.getGoodRP(relId);
            int badRP = articleService.getBadRP(relId);
            return ResultData.from("S-2", "싫어요 했었음", "goodRP", goodRP, "badRP", badRP);
        }

        ResultData reactionRd = reactionPointService.addGoodReactionPoint(rq.getLoginedMemberId(), relTypeCode, relId);

        if (reactionRd.isFail()) {
            return ResultData.from(reactionRd.getResultCode(), reactionRd.getMsg());
        }

        int goodRP = articleService.getGoodRP(relId);
        int badRP = articleService.getBadRP(relId);

        return ResultData.from(reactionRd.getResultCode(), reactionRd.getMsg(), "goodRP", goodRP, "badRP", badRP);
    }

    @RequestMapping("/usr/reactionPoint/doBadReaction")
    @ResponseBody
    public ResultData doBadReaction(String relTypeCode, int relId, String replaceUri) {

        ResultData usersReactionRd = reactionPointService.usersReaction(rq.getLoginedMemberId(), relTypeCode, relId);

        int usersReaction = (int) usersReactionRd.getData1();

        if (usersReaction == -1) {
            ResultData rd = reactionPointService.deleteBadReactionPoint(rq.getLoginedMemberId(), relTypeCode, relId);
            int goodRP = articleService.getGoodRP(relId);
            int badRP = articleService.getBadRP(relId);
            return ResultData.from("S-1", "싫어요 취소", "goodRP", goodRP, "badRP", badRP);
        } else if (usersReaction == 1) {
            ResultData rd = reactionPointService.deleteGoodReactionPoint(rq.getLoginedMemberId(), relTypeCode, relId);
            rd = reactionPointService.addBadReactionPoint(rq.getLoginedMemberId(), relTypeCode, relId);
            int goodRP = articleService.getGoodRP(relId);
            int badRP = articleService.getBadRP(relId);
            return ResultData.from("S-2", "좋아요 했었음", "goodRP", goodRP, "badRP", badRP);
        }

        ResultData reactionRd = reactionPointService.addBadReactionPoint(rq.getLoginedMemberId(), relTypeCode, relId);

        if (reactionRd.isFail()) {
            return ResultData.from(reactionRd.getResultCode(), reactionRd.getMsg());
        }

        int goodRP = articleService.getGoodRP(relId);
        int badRP = articleService.getBadRP(relId);

        return ResultData.from(reactionRd.getResultCode(), reactionRd.getMsg(), "goodRP", goodRP, "badRP", badRP);
    }

}