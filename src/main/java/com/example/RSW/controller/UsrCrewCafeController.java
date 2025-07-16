package com.example.RSW.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
import com.example.RSW.service.WalkCrewService;

import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.util.Date;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequestMapping("/usr/crewCafe")
@Controller
public class UsrCrewCafeController {

	@Autowired
	private ArticleService articleService;

	@Autowired
	private WalkCrewService walkCrewService;

	@GetMapping("")
	public String showCafeMain(@RequestParam(required = false) Integer crewId, Model model) {

		return "forward:/usr/crewCafe/cafeHome?crewId=" + crewId; // 이 JSP 경로가 존재해야 함
	}

	// 까페홈에 article 글 보이게 하기
	@GetMapping("/cafeHome")
	public String showCafeHome(@RequestParam int crewId, Model model, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");
		WalkCrew crew = walkCrewService.getCrewById(crewId);

		// ✅ 게시판 ID 기준으로 불러오기
		int noticeBoardId = 1; // 공지사항
		int freeBoardId = 3; // 자유게시판
		int galleryBoardId = 4; // 사진첩

		/*
		 * List<Article> noticeArticles =
		 * articleService.getRecentArticlesByCrewAndBoardId(crewId, noticeBoardId, 5);
		 * List<Article> freeArticles =
		 * articleService.getRecentArticlesByCrewAndBoardId(crewId, freeBoardId, 5);
		 * List<Article> galleryArticles =
		 * articleService.getRecentArticlesByCrewAndBoardId(crewId, galleryBoardId, 5);
		 */

		// 로그용
		System.out.println("✅ crewId = " + crewId);

		List<Article> noticeArticles = articleService.getRecentArticlesByCrewAndBoardId(crewId, 1, 5);
		System.out.println("✅ noticeArticles.size = " + noticeArticles.size());

		List<Article> freeArticles = articleService.getRecentArticlesByCrewAndBoardId(crewId, 3, 5);
		System.out.println("✅ freeArticles.size = " + freeArticles.size());
		for (Article a : freeArticles) {
			System.out.println("📝 자유글: id=" + a.getId() + ", title=" + a.getTitle());
		}

		List<Article> galleryArticles = articleService.getRecentArticlesByCrewAndBoardId(crewId, 4, 5);
		System.out.println("✅ galleryArticles.size = " + galleryArticles.size());

		// 여기까지
		model.addAttribute("crew", crew);
		model.addAttribute("noticeArticles", noticeArticles);
		model.addAttribute("freeArticles", freeArticles);
		model.addAttribute("galleryArticles", galleryArticles);

		return "usr/crewCafe/cafeHome";
	}

}
