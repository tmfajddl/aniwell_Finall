package com.example.RSW.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.example.RSW.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.RSW.interceptor.BeforeActionInterceptor;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Article;
import com.example.RSW.vo.Board;
import com.example.RSW.vo.Reply;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Rq;
import com.example.RSW.vo.WalkCrew;

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
	private WalkCrewService walkCrewService;

	@Autowired
	private ReactionPointService reactionPointService;

	@Autowired
	private ReplyService replyService;

	@Autowired
	private Cloudinary cloudinary;

	UsrArticleController(BeforeActionInterceptor beforeActionInterceptor) {
		this.beforeActionInterceptor = beforeActionInterceptor;
	}

	@ResponseBody
	@GetMapping("/usr/article/write/check")
	public ResultData checkWritePermission(HttpServletRequest req, @RequestParam(required = false) Integer boardId,
			@RequestParam(required = false) Integer crewId, @RequestParam(required = false) String type) {

		Rq rq = (Rq) req.getAttribute("rq");

		if (crewId != null) {
			WalkCrew crew = walkCrewService.getCrewById(crewId);

			if (crew == null) {
				return ResultData.from("F-1", "존재하지 않는 크루입니다.");
			}

			boolean isApproved = walkCrewService.isApprovedMember(crewId, rq.getLoginedMemberId());
			if (!isApproved) {
				return ResultData.from("F-2", "승인된 크루 멤버만 글쓰기 가능합니다.");
			}

			if (boardId != null && boardId == 1) {
				boolean isLeader = walkCrewService.isCrewLeader(crewId, rq.getLoginedMemberId());
				if (!isLeader) {
					return ResultData.from("F-3", "공지사항은 크루장만 작성할 수 있습니다.");
				}
			}

			return ResultData.from("S-1", "글쓰기 권한 확인 성공",
					Map.of("crewId", crewId, "boardId", boardId, "type", type, "crewName", crew.getTitle()));
		}

		// 일반 게시판인 경우 기본 boardId 할당
		if (boardId == null) {
			boardId = 2;
		}

		return ResultData.from("S-2", "일반 게시판 글쓰기 가능", Map.of("boardId", boardId, "type", type));
	}

	@PostMapping("/usr/article/doWrite")
	@ResponseBody
	public ResultData doWrite(HttpServletRequest req, @RequestParam(required = false) Integer crewId,
			@RequestParam(required = false) Integer boardId, @RequestParam String title, @RequestParam String body,
			@RequestParam(required = false) MultipartFile imageFile) {

		Rq rq = (Rq) req.getAttribute("rq");
		int loginedMemberId = rq.getLoginedMemberId();

		String imageUrl = null;

		// ✅ 이미지 업로드 처리 (Cloudinary)
		if (imageFile != null && !imageFile.isEmpty()) {
			try {
				Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
				imageUrl = (String) uploadResult.get("secure_url");
				System.out.println("✅ 업로드 성공: " + imageUrl);
			} catch (IOException e) {
				e.printStackTrace();
				return ResultData.from("F-Img", "이미지 업로드 실패");
			}
		}

		// ✅ 게시글 작성 처리
		ResultData rd;
		if (crewId != null) {
			rd = articleService.writeCrewArticle(boardId, crewId, loginedMemberId, title, body, imageUrl);
		} else {
			rd = articleService.writeArticle(loginedMemberId, title, body, String.valueOf(boardId), imageUrl);
		}

		if (rd.isFail()) {
			return ResultData.from(rd.getResultCode(), rd.getMsg());
		}

		// ✅ 생성된 게시글 ID 및 이동 URL 포함 응답
		int articleId = (int) rd.getData1();
		String redirectUrl = crewId != null ? "/usr/article/detail?id=" + articleId + "&crewId=" + crewId
				: "/usr/article/detail?id=" + articleId + "&boardId=" + boardId;

		return ResultData.from("S-1", "게시글이 성공적으로 작성되었습니다.",
				Map.of("articleId", articleId, "redirectUrl", redirectUrl));
	}

	// ✅ 게시글 수정 처리 (JSON 방식)
	@PostMapping("/usr/article/doModify")
	@ResponseBody
	public ResultData doModify(HttpServletRequest req, @RequestBody Map<String, Object> param) {
		Rq rq = (Rq) req.getAttribute("rq");

		int id = (int) param.get("id");
		String title = (String) param.get("title");
		String body = (String) param.get("body");

		// 게시글 조회
		Article article = articleService.getArticleById(id);
		if (article == null) {
			return ResultData.from("F-1", id + "번 게시글은 존재하지 않습니다.");
		}

		// 수정 권한 확인
		ResultData userCanModifyRd = articleService.userCanModify(rq.getLoginedMemberId(), article);
		if (userCanModifyRd.isFail()) {
			return ResultData.from(userCanModifyRd.getResultCode(), userCanModifyRd.getMsg());
		}

		// 게시글 수정 처리
		articleService.modifyArticle(id, title, body);

		// 성공 응답
		return ResultData.from("S-1", "게시글 수정이 완료되었습니다.", Map.of("redirectUrl", "/usr/article/detail?id=" + id));
	}

	// ✅ 게시글 수정 폼 데이터 조회 (JSON 방식)
	@GetMapping("/usr/article/modify")
	@ResponseBody
	public ResultData showModify(HttpServletRequest req, @RequestParam int id) {
		Rq rq = (Rq) req.getAttribute("rq");

		// 로그인 여부 확인
		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-0", "로그인 후 이용해주세요.");
		}

		// 게시글 조회
		Article article = articleService.getArticleById(id);
		if (article == null) {
			return ResultData.from("F-1", "존재하지 않는 게시물입니다.");
		}

		// 수정 권한 확인
		ResultData userCanModifyRd = articleService.userCanModify(rq.getLoginedMemberId(), article);
		if (userCanModifyRd.isFail()) {
			return ResultData.from(userCanModifyRd.getResultCode(), userCanModifyRd.getMsg());
		}

		// JSON 형태로 수정 폼에 필요한 정보 제공
		return ResultData.from("S-1", "게시글 수정 정보 조회 성공", Map.of("id", article.getId(), "title", article.getTitle(),
				"body", article.getBody(), "boardId", article.getBoardId() // crewId 없이
		));
	}

	@PostMapping("/usr/article/doDelete")
	@ResponseBody
	public ResultData doDelete(HttpServletRequest req, @RequestParam int id, @RequestParam int crewId) {
		Rq rq = (Rq) req.getAttribute("rq");

		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-0", "로그인 후 이용해주세요.");
		}

		Article article = articleService.getArticleById(id);
		if (article == null) {
			return ResultData.from("F-1", id + "번 게시글은 존재하지 않습니다.");
		}

		ResultData userCanDeleteRd = articleService.userCanDelete(rq.getLoginedMemberId(), article);
		if (userCanDeleteRd.isFail()) {
			return ResultData.from(userCanDeleteRd.getResultCode(), userCanDeleteRd.getMsg());
		}

		articleService.deleteArticle(id);

		// ✅ 프론트에서 리디렉션할 수 있도록 리턴
		return ResultData.from("S-1", "게시글이 삭제되었습니다.",
				Map.of("redirectUrl", "/usr/crewCafe/cafeHome?crewId=" + crewId));
	}

	@GetMapping("/usr/article/detail")
	@ResponseBody
	public ResultData showDetail(HttpServletRequest req, @RequestParam int id,
			@RequestParam(required = false) Integer crewId, @RequestParam(required = false) Integer boardId) {

		Rq rq = (Rq) req.getAttribute("rq");
		int loginedMemberId = rq != null ? rq.getLoginedMemberId() : 0;

		// ✅ 게시글 정보
		Article article = articleService.getForPrintArticle(loginedMemberId, id);
		if (article == null) {
			return ResultData.from("F-1", "해당 게시글이 존재하지 않습니다.");
		}

		// ✅ 리액션 여부
		ResultData usersReactionRd = reactionPointService.usersReaction(loginedMemberId, "article", id);
		boolean userCanMakeReaction = usersReactionRd.isSuccess();

		// ✅ 좋아요 / 싫어요 여부
		boolean isAlreadyAddGoodRp = reactionPointService.isAlreadyAddGoodRp(loginedMemberId, id, "article");
		boolean isAlreadyAddBadRp = reactionPointService.isAlreadyAddBadRp(loginedMemberId, id, "article");

		// ✅ 댓글 목록
		List<Reply> replies = replyService.getForPrintReplies(loginedMemberId, "article", id);

		// ✅ 크루 또는 게시판 정보
		Map<String, Object> extra = new java.util.HashMap<>();
		if (crewId != null) {
			WalkCrew crew = walkCrewService.getCrewById(crewId);
			if (crew != null) {
				extra.put("crew", crew);
			}
		} else if (boardId != null) {
			Board board = boardService.getBoardById(boardId);
			if (board != null) {
				extra.put("board", board);
			}
		}

		// ✅ 응답 데이터 구성
		Map<String, Object> data = Map.of("article", article, "replies", replies, "repliesCount", replies.size(),
				"userCanMakeReaction", userCanMakeReaction, "isAlreadyAddGoodRp", isAlreadyAddGoodRp,
				"isAlreadyAddBadRp", isAlreadyAddBadRp, "usersReaction", usersReactionRd.getData1(), "extra", extra);

		return ResultData.from("S-1", "게시글 상세 정보 조회 성공", data);
	}

	@GetMapping("/usr/article/list")
	@ResponseBody
	public ResultData showList(HttpServletRequest req, @RequestParam(required = false) Integer boardId,
			@RequestParam(required = false) Integer crewId, @RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "title") String searchKeywordTypeCode,
			@RequestParam(defaultValue = "") String searchKeyword) throws IOException {

		Rq rq = (Rq) req.getAttribute("rq");

		// ✅ crewId와 boardId 모두 존재하는 경우 (크루 게시판)
		if (crewId != null && boardId != null) {
			WalkCrew crew = walkCrewService.getCrewById(crewId);
			Board board = boardService.getBoardById(boardId);
			if (crew == null || board == null) {
				return ResultData.from("F-1", "존재하지 않는 크루 또는 게시판입니다.");
			}

			List<Article> articles = articleService.getArticlesByCrewIdAndBoardId(crewId, boardId);

			return ResultData.from("S-1", "크루 게시판 글 목록 조회 성공",
					Map.of("crew", crew, "board", board, "articles", articles, "page", page));
		}

		// ✅ crewId만 존재하는 경우 (크루 전체 글)
		if (crewId != null) {
			WalkCrew crew = walkCrewService.getCrewById(crewId);
			if (crew == null) {
				return ResultData.from("F-2", "존재하지 않는 크루입니다.");
			}

			List<Article> articles = articleService.getArticlesByCrewId(crewId);

			return ResultData.from("S-2", "크루 전체 글 목록 조회 성공", Map.of("crew", crew, "articles", articles));
		}

		// ✅ 일반 게시판
		if (boardId != null) {
			Board board = boardService.getBoardById(boardId);
			if (board == null) {
				return ResultData.from("F-3", "존재하지 않는 게시판입니다.");
			}

			int itemsInAPage = 10;
			int articlesCount;
			int pagesCount;
			List<Article> articles;

			if (boardId == 1) {
				// 전체 공지사항 (관리자 전용)
				articlesCount = articleService.getAdminOnlyArticleCount(boardId, searchKeywordTypeCode, searchKeyword);
				pagesCount = (int) Math.ceil(articlesCount / (double) itemsInAPage);
				articles = articleService.getAdminOnlyArticles(boardId, itemsInAPage * (page - 1), itemsInAPage,
						searchKeywordTypeCode, searchKeyword);
			} else {
				articlesCount = articleService.getArticleCount(boardId, searchKeywordTypeCode, searchKeyword);
				pagesCount = (int) Math.ceil(articlesCount / (double) itemsInAPage);
				articles = articleService.getForPrintArticles(boardId, itemsInAPage * (page - 1), itemsInAPage,
						searchKeywordTypeCode, searchKeyword);
			}

			return ResultData.from("S-3", "게시판 글 목록 조회 성공",
					Map.of("board", board, "articles", articles, "articlesCount", articlesCount, "pagesCount",
							pagesCount, "searchKeywordTypeCode", searchKeywordTypeCode, "searchKeyword", searchKeyword,
							"page", page));
		}

		return ResultData.from("F-4", "boardId 또는 crewId가 필요합니다.");
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

	// ✅ 모임일정 등록 (JSON 응답)
	@PostMapping("/usr/article/doWriteSchedule")
	@ResponseBody
	public ResultData doWriteSchedule(@RequestParam int crewId, @RequestParam String scheduleDate,
			@RequestParam String scheduleTitle, @RequestParam(required = false) String scheduleBody,
			HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		if (rq == null || !rq.isLogined()) {
			return ResultData.from("F-1", "로그인이 필요합니다.");
		}

		int loginedMemberId = rq.getLoginedMemberId();

		// ✅ 기존과 동일하게 저장만 처리
		articleService.writeSchedule(crewId, loginedMemberId, scheduleDate, scheduleTitle, scheduleBody);

		// ✅ 성공 메시지 리턴 (articleId 없이)
		return ResultData.from("S-1", "모임 일정이 등록되었습니다.",
				Map.of("crewId", crewId, "redirectUrl", "/usr/crewCafe/cafeHome?crewId=" + crewId));
	}

	// ✅ JSON 응답 방식으로 변경
	@GetMapping("/usr/article/schedule")
	@ResponseBody
	public ResultData showSchedule(@RequestParam int crewId) {
		List<Map<String, Object>> scheduleList = articleService.getSchedulesByCrewId(crewId);

		if (scheduleList == null || scheduleList.isEmpty()) {
			return ResultData.from("F-1", "등록된 모임 일정이 없습니다.");
		}

		return ResultData.from("S-1", "모임 일정 조회 성공", Map.of("crewId", crewId, "schedules", scheduleList));
	}

}