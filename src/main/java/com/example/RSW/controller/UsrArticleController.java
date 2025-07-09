package com.example.RSW.controller;

import java.io.IOException;
import java.util.List;

import com.example.RSW.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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


    UsrArticleController(BeforeActionInterceptor beforeActionInterceptor) {
        this.beforeActionInterceptor = beforeActionInterceptor;
    }


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

    // 로그인 체크 -> 유무 체크 -> 권한체크
    @RequestMapping("/usr/article/doModify")
    @ResponseBody
    public String doModify(HttpServletRequest req, int id, String title, String body) {

        Rq rq = (Rq) req.getAttribute("rq");

        Article article = articleService.getArticleById(id);

        if (article == null) {
            return Ut.jsReplace("F-1", Ut.f("%d번 게시글은 없습니다", id), "../article/list");
        }

        ResultData userCanModifyRd = articleService.userCanModify(rq.getLoginedMemberId(), article);

        if (userCanModifyRd.isFail()) {
            return Ut.jsHistoryBack(userCanModifyRd.getResultCode(), userCanModifyRd.getMsg());
        }

        if (userCanModifyRd.isSuccess()) {
            articleService.modifyArticle(id, title, body);
        }

        article = articleService.getArticleById(id);

        return Ut.jsReplace(userCanModifyRd.getResultCode(), userCanModifyRd.getMsg(), "../article/detail?id=" + id);
    }

    @RequestMapping("/usr/article/doDelete")
    @ResponseBody
    public String doDelete(HttpServletRequest req, int id) {

        Rq rq = (Rq) req.getAttribute("rq");

        Article article = articleService.getArticleById(id);

        if (article == null) {
            return Ut.jsHistoryBack("F-1", Ut.f("%d번 게시글은 없습니다", id));
        }

        ResultData userCanDeleteRd = articleService.userCanDelete(rq.getLoginedMemberId(), article);

        if (userCanDeleteRd.isFail()) {
            return Ut.jsHistoryBack(userCanDeleteRd.getResultCode(), userCanDeleteRd.getMsg());
        }

        if (userCanDeleteRd.isSuccess()) {
            articleService.deleteArticle(id);
        }

        return Ut.jsReplace(userCanDeleteRd.getResultCode(), userCanDeleteRd.getMsg(), "../article/list");
    }

    @RequestMapping("/usr/article/detail")
    public String showDetail(HttpServletRequest req, Model model, int id) {
        Rq rq = (Rq) req.getAttribute("rq");

        Article article = articleService.getForPrintArticle(rq.getLoginedMemberId(), id);

        ResultData usersReactionRd = reactionPointService.usersReaction(rq.getLoginedMemberId(), "article", id);

        if (usersReactionRd.isSuccess()) {
            model.addAttribute("userCanMakeReaction", usersReactionRd.isSuccess());
        }

        List<Reply> replies = replyService.getForPrintReplies(rq.getLoginedMemberId(), "article", id);

        int repliesCount = replies.size();

        model.addAttribute("replies", replies);
        model.addAttribute("repliesCount", repliesCount);

        model.addAttribute("article", article);
        model.addAttribute("usersReaction", usersReactionRd.getData1());
        model.addAttribute("isAlreadyAddGoodRp",
                reactionPointService.isAlreadyAddGoodRp(rq.getLoginedMemberId(), id, "article"));
        model.addAttribute("isAlreadyAddBadRp",
                reactionPointService.isAlreadyAddBadRp(rq.getLoginedMemberId(), id, "article"));

        return "usr/article/detail";
    }

    @RequestMapping("/usr/article/doIncreaseHitCountRd")
    @ResponseBody
    public ResultData doIncreaseHitCount(int id) {

        ResultData increaseHitCountRd = articleService.increaseHitCount(id);

        if (increaseHitCountRd.isFail()) {
            return increaseHitCountRd;
        }

        return ResultData.newData(increaseHitCountRd, "hitCount", articleService.getArticleHitCount(id));
    }

    @RequestMapping("/usr/article/write")
    public String showWrite(HttpServletRequest req) {

        return "usr/article/write";
    }

    @RequestMapping("/usr/article/doWrite")
    @ResponseBody
    public String doWrite(HttpServletRequest req, String title, String body, String boardId, Model model) {

        Rq rq = (Rq) req.getAttribute("rq");

        if (Ut.isEmptyOrNull(title)) {
            return Ut.jsHistoryBack("F-1", "제목을 입력하세요");
        }

        if (Ut.isEmptyOrNull(body)) {
            return Ut.jsHistoryBack("F-2", "내용을 입력하세요");
        }

        if (Ut.isEmptyOrNull(boardId)) {
            return Ut.jsHistoryBack("F-3", "게시판을 선택하세요");
        }

        ResultData doWriteRd = articleService.writeArticle(rq.getLoginedMemberId(), title, body, boardId);

        int id = (int) doWriteRd.getData1();

        List<Board> boards = boardService.getBoards(); // 모든 게시판 가져오기
        model.addAttribute("boards", boards); // JSP로 전달

        Article article = articleService.getArticleById(id);

        return Ut.jsReplace(doWriteRd.getResultCode(), doWriteRd.getMsg(), "../article/detail?id=" + id + "&boardId=" + boardId);
    }

    @RequestMapping("/usr/article/list")
    public String showList(HttpServletRequest req, Model model, @RequestParam(defaultValue = "1") int boardId,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "title") String searchKeywordTypeCode,
                           @RequestParam(defaultValue = "") String searchKeyword) throws IOException {

        Rq rq = (Rq) req.getAttribute("rq");

        Board board = boardService.getBoardById(boardId);

        if (board == null) {
            return rq.historyBackOnView("존재하지 않는 게시판");
        }

        int articlesCount = articleService.getArticleCount(boardId, searchKeywordTypeCode, searchKeyword);

        // 한 페이지에 글 10개씩
        // 글 20 -> 2page
        // 글 25 -> 3page
        int itemsInAPage = 10;

        int pagesCount = (int) Math.ceil(articlesCount / (double) itemsInAPage);

        List<Article> articles = articleService.getForPrintArticles(boardId, itemsInAPage, page, searchKeywordTypeCode,
                searchKeyword);

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