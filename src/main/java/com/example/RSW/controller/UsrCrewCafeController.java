package com.example.RSW.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.RSW.vo.Rq;
import com.example.RSW.vo.WalkCrew;
import com.example.RSW.vo.District;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Article;
import com.example.RSW.util.Ut;
import com.example.RSW.config.AppConfig;
import com.example.RSW.repository.DistrictRepository;
import com.example.RSW.service.ArticleService;
import com.example.RSW.service.DistrictService;
import com.example.RSW.service.MemberService;
import com.example.RSW.service.WalkCrewMemberService;
import com.example.RSW.service.WalkCrewService;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequestMapping("/usr/crewCafe")
@RestController
public class UsrCrewCafeController {

	@Autowired
	private ArticleService articleService;

	@Autowired
	private WalkCrewService walkCrewService;

	@Autowired
	private WalkCrewMemberService walkCrewMemberService;

	@Autowired
	private Cloudinary cloudinary;

	@GetMapping("")
	public ResultData index(@RequestParam(required = false) Integer crewId) {
		if (crewId == null) {
			return ResultData.from("F-1", "crewId가 필요합니다.");
		}
		return ResultData.from("S-1", "크루 홈으로 이동 가능", "crewId", crewId);
	}

	// 까페홈에 article 글 보이게 하기
	@GetMapping("/home")
	public ResultData getCafeHome(@RequestParam int crewId) {
		WalkCrew crew = walkCrewService.getCrewById(crewId);
		if (crew == null) {
			return ResultData.from("F-1", "크루 정보가 없습니다.");
		}

		// ✅ 게시판 ID 기준으로 불러오기
		int noticeBoardId = 1; // 공지사항
		int freeBoardId = 3; // 자유게시판
		int galleryBoardId = 4; // 사진첩
		int scheduleBoardId = 5; // 일정 게시판

		// ✅ 공지글 5개
		List<Article> noticeArticles = articleService.getRecentArticlesByCrewAndBoardId(crewId, noticeBoardId, 5);

		// ✅ 자유글 5개
		List<Article> freeArticles = articleService.getRecentArticlesByCrewAndBoardId(crewId, freeBoardId, 5);

		// ✅ 사진용 게시글: 사진첩(boardId=4) 중 imageUrl이 있는 글만 최대 20개
		List<Article> galleryArticles = articleService.getRecentArticlesByCrewAndBoardId(crewId, galleryBoardId, 20)
				.stream().filter(a -> a.getImageUrl() != null && !a.getImageUrl().isEmpty()
						&& !"undefined".equals(a.getImageUrl()))
				.collect(Collectors.toList());

		// ✅ 일정모임 리스트 불러오기
		List<Article> scheduleArticles = articleService.getRecentArticlesByCrewAndBoardId(crewId, scheduleBoardId, 10);

		// ✅ Java 8 대응: Map.of(...) 대신 new HashMap<>() + put() 사용
		Map<String, Object> data = new HashMap<>();
		data.put("crew", crew);
		data.put("noticeArticles", noticeArticles);
		data.put("freeArticles", freeArticles);
		data.put("galleryArticles", galleryArticles);
		data.put("scheduleArticles", scheduleArticles);

		return ResultData.from("S-1", "카페 콘텐츠 불러오기 성공", data);
	}

	// ✅ 내가 가입한 크루의 카페로 이동
	@GetMapping("/myCrewCafe")
	public ResultData getMyCrewCafe(HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");
		int memberId = rq.getLoginedMemberId();

		WalkCrew myCrew = walkCrewService.getCrewByLeaderId(memberId);
		if (myCrew == null) {
			myCrew = walkCrewMemberService.getMyCrew(memberId);
		}

		if (myCrew == null) {
			return ResultData.from("F-1", "가입된 크루가 없습니다.");
		}

		// ✅ 기존 model.addAttribute("crew", ...), model.addAttribute("articles", ...) 내용을
		// JSON으로 반환
		List<Article> articles = articleService.getArticlesByCrewId(myCrew.getId());

		Map<String, Object> data = new HashMap<>();
		data.put("crew", myCrew);
		data.put("articles", articles);

		return ResultData.from("S-1", "내 크루 카페 정보 반환 성공", data);
	}

}
