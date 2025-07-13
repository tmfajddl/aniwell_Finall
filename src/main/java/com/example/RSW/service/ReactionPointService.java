package com.example.RSW.service;

import com.example.RSW.repository.ArticleRepository;
import com.example.RSW.repository.MemberRepository;
import com.example.RSW.vo.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.RSW.repository.BoardRepository;
import com.example.RSW.repository.ReactionPointRepository;
import com.example.RSW.vo.Board;
import com.example.RSW.vo.ResultData;

@Service
public class ReactionPointService {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ReactionPointRepository reactionPointRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationService notificationService;

    public ReactionPointService(ReactionPointRepository reactionPointRepository) {
        this.reactionPointRepository = reactionPointRepository;
    }

    public ResultData usersReaction(int loginedMemberId, String relTypeCode, int relId) {

        if (loginedMemberId == 0) {
            return ResultData.from("F-L", "로그인 하고 써야해");
        }

        int sumReactionPointByMemberId = reactionPointRepository.getSumReactionPoint(loginedMemberId, relTypeCode,
                relId);

        if (sumReactionPointByMemberId != 0) {
            return ResultData.from("F-1", "추천 불가능", "sumReactionPointByMemberId", sumReactionPointByMemberId);
        }

        return ResultData.from("S-1", "추천 가능", "sumReactionPointByMemberId", sumReactionPointByMemberId);
    }

    public ResultData addGoodReactionPoint(int loginedMemberId, String relTypeCode, int relId) {

        int affectedRow = reactionPointRepository.addGoodReactionPoint(loginedMemberId, relTypeCode, relId);

        if (affectedRow != 1) {
            return ResultData.from("F-1", "좋아요 실패");
        }

        switch (relTypeCode) {
            case "article":
                articleService.increaseGoodReactionPoint(relId);
                break;
        }

        return ResultData.from("S-1", "좋아요!");
    }

    public ResultData addBadReactionPoint(int loginedMemberId, String relTypeCode, int relId) {
        int affectedRow = reactionPointRepository.addBadReactionPoint(loginedMemberId, relTypeCode, relId);

        if (affectedRow != 1) {
            return ResultData.from("F-1", "싫어요 실패");
        }

        switch (relTypeCode) {
            case "article":
                articleService.increaseBadReactionPoint(relId);
                break;
        }

        return ResultData.from("S-1", "싫어요!");
    }

    public ResultData deleteGoodReactionPoint(int loginedMemberId, String relTypeCode, int relId) {
        reactionPointRepository.deleteReactionPoint(loginedMemberId, relTypeCode, relId);

        switch (relTypeCode) {
            case "article":
                articleService.decreaseGoodReactionPoint(relId);
                break;
        }
        return ResultData.from("S-1", "좋아요 취소 됨");

    }

    public ResultData deleteBadReactionPoint(int loginedMemberId, String relTypeCode, int relId) {
        reactionPointRepository.deleteReactionPoint(loginedMemberId, relTypeCode, relId);

        switch (relTypeCode) {
            case "article":
                articleService.decreaseBadReactionPoint(relId);
                break;
        }
        return ResultData.from("S-1", "싫어요 취소 됨");
    }

    public boolean isAlreadyAddGoodRp(int memberId, int relId, String relTypeCode) {
        int getPointTypeCodeByMemberId = reactionPointRepository.getSumReactionPoint(memberId, relTypeCode, relId);

        if (getPointTypeCodeByMemberId > 0) {
            return true;
        }

        return false;
    }

    public boolean isAlreadyAddBadRp(int memberId, int relId, String relTypeCode) {
        int getPointTypeCodeByMemberId = reactionPointRepository.getSumReactionPoint(memberId, relTypeCode, relId);

        if (getPointTypeCodeByMemberId < 0) {
            return true;
        }

        return false;
    }


    public ResultData<?> toggleReaction(int memberId, String relTypeCode, int relId) {
        boolean isReacted = reactionPointRepository.existsByMemberIdAndRelTypeCodeAndRelId(memberId, relTypeCode, relId);

        if (isReacted) {
            reactionPointRepository.delete(memberId, relTypeCode, relId);
            return ResultData.from("S-2", "좋아요 취소");
        }

        reactionPointRepository.insert(memberId, relTypeCode, relId);

        // ✅ 알림 처리
        if (relTypeCode.equals("post")) {
            Article article = articleRepository.getArticleById(relId);
            if (article != null && article.getMemberId() != memberId) {
                String nickname = memberRepository.getNicknameById(memberId);
                String message = "❤️ " + nickname + "님이 회원님의 글에 좋아요를 눌렀습니다.";
                String link = "/usr/post/detail?id=" + relId;

                notificationService.notifyMember(article.getMemberId(), message, link);
            }
        }

        return ResultData.from("S-1", "좋아요 성공");
    }
}