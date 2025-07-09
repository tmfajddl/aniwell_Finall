package com.example.RSW.controller;

import java.util.List;

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
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.util.Ut;
import com.example.RSW.config.AppConfig;
import com.example.RSW.service.MemberService;
import com.example.RSW.service.WalkCrewService;

import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.util.Date;

@Controller
@RequestMapping("/usr/walkCrew")
public class UsrWalkCrewController {

	private final WalkCrewService walkCrewService;

	@Autowired
	public UsrWalkCrewController(WalkCrewService walkCrewService) {
		this.walkCrewService = walkCrewService;
	}

	// 크루 목록 페이지 이동 (예: /usr/walkCrew/list)
	@GetMapping("/list")
	public String showCrewList(Model model) {
		List<WalkCrew> crews = walkCrewService.getAllCrews();
		model.addAttribute("crews", crews);
		return "usr/walkCrew/list"; // => /WEB-INF/views/usr/walkCrew/list.jsp
	}

	// 크루 등록 폼 페이지
	@Autowired
	private AppConfig appConfig; // @Value 주입된 클래스

	@GetMapping("/create")
	public String showCreateForm(Model model) {
		model.addAttribute("kakaoJsKey", appConfig.getKakaoJavascriptKey()); // JSP에서 사용될 키
		return "usr/walkCrew/create";
	}

	// 크루 등록 처리
	@PostMapping("/doCreate")
	public String doCreate(WalkCrew walkCrew) {
		walkCrewService.createCrew(walkCrew);
		return "redirect:/usr/walkCrew/list";
	}

	// 크루 상세보기 페이지

	@GetMapping("/detail/{id}")
	public String showDetail(@PathVariable int id, Model model) {
		WalkCrew crew = walkCrewService.getCrewById(id);

		// ✅ createdAt → Date 변환
		Date createdDate = Date.from(crew.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());

		model.addAttribute("crew", crew);
		model.addAttribute("createdDate", createdDate); // JSP에서 사용할 Date 객체

		return "usr/walkCrew/detail";
	}

}