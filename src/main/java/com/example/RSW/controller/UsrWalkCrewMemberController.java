package com.example.RSW.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.RSW.service.WalkCrewMemberService;
import com.example.RSW.service.WalkCrewService;
import com.example.RSW.vo.Rq;
import com.example.RSW.vo.WalkCrew;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/usr/walkCrewMember")
public class UsrWalkCrewMemberController {

	private final WalkCrewService walkCrewService;
	private final WalkCrewMemberService walkCrewMemberService;

	@Autowired
	public UsrWalkCrewMemberController(WalkCrewService walkCrewService, WalkCrewMemberService walkCrewMemberService) {
		this.walkCrewService = walkCrewService;
		this.walkCrewMemberService = walkCrewMemberService;
	}

	// ✅ 크루 메뉴용 공통 데이터
	@ModelAttribute("crew")
	public WalkCrew getCrewForMenu(HttpServletRequest req) {
		Rq rq = (Rq) req.getAttribute("rq");

		if (rq == null || !rq.isLogined())
			return null;

		int memberId = rq.getLoginedMemberId();

		// 크루장
		WalkCrew crew = walkCrewService.getCrewByLeaderId(memberId);
		if (crew != null)
			return crew;

		// 참가 승인된 멤버
		return walkCrewService.getCrewByMemberId(memberId);
	}

	// ✅ 참가 요청 (예시)
	@PostMapping("/doJoin")
	public String doJoin(@RequestParam int crewId, HttpServletRequest req, Model model) {
		Rq rq = (Rq) req.getAttribute("rq");

		if (rq == null || !rq.isLogined()) {
			model.addAttribute("errorMsg", "로그인이 필요합니다.");
			return "common/error";
		}

		int memberId = rq.getLoginedMemberId();
		walkCrewMemberService.requestToJoinCrew(crewId, memberId);

		return "redirect:/usr/walkCrew/detail?id=" + crewId;
	}

	// ✅ 신청자 리스트 (크루장만)
	@GetMapping("/requestList")
	public String showRequestList(@RequestParam int crewId, HttpServletRequest req, Model model) {
		Rq rq = (Rq) req.getAttribute("rq");
		if (!rq.isLogined()) {
			return "redirect:/usr/member/login?msg=로그인 후 이용해주세요.";
		}

		int loginedMemberId = rq.getLoginedMemberId();
		WalkCrew crew = walkCrewService.getCrewById(crewId);

		if (crew.getLeaderId() != loginedMemberId) {
			return "redirect:/usr/walkCrew/detail/" + crewId + "?msg=해당 페이지에 접근 권한이 없습니다.";
		}

		// 신청자 리스트 조회
		List<Map<String, Object>> applicants = walkCrewService.getApplicantsByCrewId(crewId);
		model.addAttribute("applicants", applicants);
		model.addAttribute("crewId", crewId);

		return "usr/walkCrewMember/requestList";
	}

	// ✅ 신청자 상세 정보 보기
	@GetMapping("/requestDetail")
	public String showRequestDetail(@RequestParam int crewId, @RequestParam int memberId, HttpServletRequest req,
			Model model) {
		Rq rq = (Rq) req.getAttribute("rq");

		if (!rq.isLogined()) {
			return "redirect:/usr/member/login?msg=로그인 후 이용해주세요.";
		}

		// 크루장인지 확인
		WalkCrew crew = walkCrewService.getCrewById(crewId);
		if (crew.getLeaderId() != rq.getLoginedMemberId()) {
			return "redirect:/usr/walkCrew/detail/" + crewId + "?msg=해당 페이지에 접근 권한이 없습니다.";
		}

		// 신청자 정보 가져오기
		Map<String, Object> applicant = walkCrewService.getApplicantDetail(crewId, memberId);
		if (applicant == null) {
			return "redirect:/usr/walkCrew/requestList?crewId=" + crewId + "&msg=신청자 정보를 찾을 수 없습니다.";
		}

		model.addAttribute("applicant", applicant);
		model.addAttribute("crewId", crewId);

		return "usr/walkCrewMember/requestDetail";
	}

}
