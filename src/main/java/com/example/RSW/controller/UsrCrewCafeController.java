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
	ArticleService articleService;

	@Autowired
	WalkCrewService walkCrewService;

	@GetMapping("")
	public String showCafeMain(@RequestParam(required = false) Integer crewId, Model model) {

		return "usr/crewCafe/cafeHome"; // 이 JSP 경로가 존재해야 함
	}

	// 까페홈에 article 글 보이게 하기
	@GetMapping("/cafeHome")
	public String showCafeHome(@RequestParam int crewId, Model model, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");
		WalkCrew crew = walkCrewService.getCrewById(crewId);

		// 최신글 5개씩 각 타입별 가져오기
		List<Article> noticeArticles = articleService.getRecentArticlesByCrewAndType(crewId, "notice", 5);
		List<Article> freeArticles = articleService.getRecentArticlesByCrewAndType(crewId, "free", 5);
		List<Article> galleryArticles = articleService.getRecentArticlesByCrewAndType(crewId, "gallery", 5);

		model.addAttribute("crew", crew);
		model.addAttribute("noticeArticles", noticeArticles);
		model.addAttribute("freeArticles", freeArticles);
		model.addAttribute("galleryArticles", galleryArticles);

		return "usr/crewCafe/cafeHome";
	}

}
