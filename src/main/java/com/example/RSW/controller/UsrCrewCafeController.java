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
import com.example.RSW.util.Ut;
import com.example.RSW.config.AppConfig;
import com.example.RSW.repository.DistrictRepository;
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
	private WalkCrewService walkCrewService;

	@GetMapping("")
	public String showCafeMain(@RequestParam int crewId, Model model, HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		// ⚠️ 개발 중이라면 임시로 권한 체크 생략
		boolean isApproved = true;

		// 실제 배포 시 아래 코드 다시 활성화
		// boolean isApproved = walkCrewService.isApprovedMember(crewId,
		// rq.getLoginedMemberId());
		if (!isApproved) {
			return "common/permissionDenied";
		}

		WalkCrew crew = walkCrewService.getCrewById(crewId);
		model.addAttribute("crew", crew);

		return "usr/crewCafe/cafeHome";
	}
}