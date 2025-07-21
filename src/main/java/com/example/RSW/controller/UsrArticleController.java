package com.example.RSW.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.RSW.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.example.RSW.interceptor.BeforeActionInterceptor;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Article;
import com.example.RSW.vo.Board;
import com.example.RSW.vo.Reply;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Rq;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsrArticleController {

    private final BeforeActionInterceptor beforeActionInterceptor;

    @Autowired
    private Rq rq;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private BoardService boardService;

    @Autowired
    private ReactionPointService reactionPointService;

    @Autowired
    private ReplyService replyService;

    @Autowired
    private Cloudinary cloudinary;

    // 생성자 주입 (BeforeActionInterceptor)
    UsrArticleController(BeforeActionInterceptor beforeActionInterceptor) {
        this.beforeActionInterceptor = beforeActionInterceptor;
    }

    // 게시글 수정 폼
    @RequestMapping("/usr/article/modify")
    public String showModify(HttpServletRequest req, Model model, int id) {
        Rq rq = (Rq) req.getAttribute("rq");
        Article article = articleService.getForPrintArticle(rq.getLoginedMemberId(), id);

        if (article == null) {
            return Ut.jsHistoryBack("F-1", Ut.f("%d번 게시글은 없습니다", id));
        }

        model.addAttribute("article", article);
        return "/usr/article/modify";
    }

    // 게시글 수정 처리
    @RequestMapping("/usr/article/doModify")
    @ResponseBody
    public ResultData doModify(HttpServletRequest req, int id, String title, String body) {
        Rq rq = (Rq) req.getAttribute("rq");
        Article article = articleService.getArticleById(id);

        if (article == null) {
            return ResultData.from("F-1", id + "번 게시글이 존재하지 않습니다.");
        }

        ResultData userCanModifyRd = articleService.userCanModify(rq.getLoginedMemberId(), article);
        if (userCanModifyRd.isFail()) {
            return userCanModifyRd;
        }

        articleService.modifyArticle(id, title, body);
        Article updatedArticle = articleService.getArticleById(id);

        return ResultData.from("S-1", "게시글이 수정되었습니다.", "article", articleService.getArticleById(id));

    }


    // 게시글 삭제 처리
    @RequestMapping("/usr/article/doDelete")
    @ResponseBody
    public ResultData doDelete(HttpServletRequest req, int id) {
        Rq rq = (Rq) req.getAttribute("rq");
        Article article = articleService.getArticleById(id);

        if (article == null) {
            return ResultData.from("F-1", id + "번 게시글은 존재하지 않습니다.");
        }

        ResultData userCanDeleteRd = articleService.userCanDelete(rq.getLoginedMemberId(), article);
        if (userCanDeleteRd.isFail()) {
            return userCanDeleteRd;
        }

        articleService.deleteArticle(id);
        return ResultData.from("S-1", "게시물이 삭제되었습니다.");
    }

    // 게시글 상세 보기
    @RequestMapping("/usr/article/detail")
    public String showDetail(HttpServletRequest req, Model model, int id) {
        Rq rq = (Rq) req.getAttribute("rq");
        Article article = articleService.getForPrintArticle(rq.getLoginedMemberId(), id);

        // 사용자 리액션 상태 확인 (좋아요/싫어요)
        ResultData usersReactionRd = reactionPointService.usersReaction(rq.getLoginedMemberId(), "article", id);
        if (usersReactionRd.isSuccess()) {
            model.addAttribute("userCanMakeReaction", true);
        }

        // 댓글 조회
        List<Reply> replies = replyService.getForPrintReplies(rq.getLoginedMemberId(), "article", id);
        model.addAttribute("replies", replies);
        model.addAttribute("repliesCount", replies.size());

        model.addAttribute("article", article);
        model.addAttribute("usersReaction", usersReactionRd.getData1());
        model.addAttribute("isAlreadyAddGoodRp", reactionPointService.isAlreadyAddGoodRp(rq.getLoginedMemberId(), id, "article"));
        model.addAttribute("isAlreadyAddBadRp", reactionPointService.isAlreadyAddBadRp(rq.getLoginedMemberId(), id, "article"));

        return "usr/article/detail";
    }

    // 조회수 증가 처리 (AJAX)
    @RequestMapping("/usr/article/doIncreaseHitCountRd")
    @ResponseBody
    public ResultData doIncreaseHitCount(int id) {
        ResultData increaseHitCountRd = articleService.increaseHitCount(id);
        if (increaseHitCountRd.isFail()) {
            return increaseHitCountRd;
        }

        return ResultData.newData(increaseHitCountRd, "hitCount", articleService.getArticleHitCount(id));
    }

    // 게시글 작성 폼
    @RequestMapping("/usr/article/write")
    public String showWrite(HttpServletRequest req) {
        return "usr/article/write";
    }

    // 게시글 작성 처리
    @PostMapping("/usr/article/doWrite")
    @ResponseBody
    public String doWrite(HttpServletRequest req, @RequestParam(required = false) Integer crewId,
                          @RequestParam(required = false) Integer boardId, @RequestParam String title, @RequestParam String body,
                          @RequestParam(required = false) MultipartFile imageFile) {

        Rq rq = (Rq) req.getAttribute("rq");
        int loginedMemberId = rq.getLoginedMemberId();

        String imageUrl = null;

        // ✅ 이미지 파일이 있다면 Cloudinary 업로드 시도
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
                imageUrl = (String) uploadResult.get("secure_url");
                System.out.println("✅ 업로드 성공: " + imageUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // ✅ 크루 글과 일반 글 구분 처리
        ResultData rd;
        if (crewId != null) {

            rd = articleService.writeCrewArticle(boardId, crewId, loginedMemberId, title, body, imageUrl);

            return Ut.jsReplace(rd.getResultCode(), rd.getMsg(),
                    "../article/detail?id=" + rd.getData1() + "&crewId=" + crewId);
        } else {
            rd = articleService.writeArticle(loginedMemberId, title, body, String.valueOf(boardId), imageUrl);
            return Ut.jsReplace(rd.getResultCode(), rd.getMsg(),
                    "../article/detail?id=" + rd.getData1() + "&boardId=" + boardId);
        }
    }



    // 게시글 리스트 페이지
    @RequestMapping("/usr/article/list")
    public String showList(HttpServletRequest req, Model model,
                           @RequestParam(defaultValue = "1") int boardId,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "title") String searchKeywordTypeCode,
                           @RequestParam(defaultValue = "") String searchKeyword) throws IOException {

        Rq rq = (Rq) req.getAttribute("rq");

        // 게시판 정보 조회
        Board board = boardService.getBoardById(boardId);
        if (board == null) {
            return rq.historyBackOnView("존재하지 않는 게시판");
        }

        // 검색 및 페이징 처리
        int articlesCount = articleService.getArticleCount(boardId, searchKeywordTypeCode, searchKeyword);
        int itemsInAPage = 10;
        int pagesCount = (int) Math.ceil(articlesCount / (double) itemsInAPage);

        List<Article> articles = articleService.getForPrintArticles(boardId, itemsInAPage, page, searchKeywordTypeCode, searchKeyword);

        // 모델에 데이터 추가
        model.addAttribute("pagesCount", pagesCount);
        model.addAttribute("articlesCount", articlesCount);
        model.addAttribute("searchKeywordTypeCode", searchKeywordTypeCode);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("articles", articles);
        model.addAttribute("boardId", boardId);
        model.addAttribute("board", board);
        model.addAttribute("page", page);

        return "usr/article/list";
    }
}