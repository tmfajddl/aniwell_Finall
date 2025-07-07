package com.example.RSW.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.RSW.vo.Rq;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.util.Ut;
import com.example.RSW.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UsrMemberController {

	@Autowired
	private Rq rq;

	@Autowired
	private MemberService memberService;

	@RequestMapping("/usr/member/doLogout")
	@ResponseBody
	public String doLogin(HttpServletRequest req) {

		Rq rq = (Rq) req.getAttribute("rq");

		rq.logout();

		return Ut.jsReplace("S-1", "로그아웃 성공", "/");
	}

	@RequestMapping("/usr/member/login")
	public String showLogin(HttpServletRequest req) {
		return "/usr/member/login";
	}

	@RequestMapping("/usr/member/doLogin")
	@ResponseBody
	public String doLogin(HttpServletRequest req, String loginId, String loginPw,
			@RequestParam(defaultValue = "/") String afterLoginUri) {

		Rq rq = (Rq) req.getAttribute("rq");

		if (Ut.isEmptyOrNull(loginId)) {
			return Ut.jsHistoryBack("F-1", "아이디를 입력해");
		}
		if (Ut.isEmptyOrNull(loginPw)) {
			return Ut.jsHistoryBack("F-2", "비밀번호를 입력해");
		}

		Member member = memberService.getMemberByLoginId(loginId);

		if (member == null) {
			return Ut.jsHistoryBack("F-3", Ut.f("%s는(은) 없는 아이디야", loginId));
		}

		System.out.println(Ut.sha256(loginPw));

		if (member.getLoginPw().equals(Ut.sha256(loginPw)) == false) {
			return Ut.jsHistoryBack("F-4", Ut.f("비밀번호가 일치하지 않습니다!!!!!"));
		}

		rq.login(member);

		return Ut.jsReplace("S-1", Ut.f("%s님 환영합니다", member.getNickname()), afterLoginUri);
	}

	@RequestMapping("/usr/member/join")
	public String showJoin(HttpServletRequest req) {
		return "/usr/member/join";
	}

	@RequestMapping("/usr/member/doJoin")
	@ResponseBody
	public String doJoin(HttpServletRequest req, String loginId, String loginPw, String name, String nickname,
			String cellphoneNum, String email) {

		if (Ut.isEmptyOrNull(loginId)) {
			return Ut.jsHistoryBack("F-1", "아이디를 입력해");
		}
		if (Ut.isEmptyOrNull(loginPw)) {
			return Ut.jsHistoryBack("F-2", "비밀번호를 입력해");

		}
		if (Ut.isEmptyOrNull(name)) {
			return Ut.jsHistoryBack("F-3", "이름을 입력해");

		}
		if (Ut.isEmptyOrNull(nickname)) {
			return Ut.jsHistoryBack("F-4", "닉네임을 입력해");

		}
		if (Ut.isEmptyOrNull(cellphoneNum)) {
			return Ut.jsHistoryBack("F-5", "전화번호를 입력해");

		}
		if (Ut.isEmptyOrNull(email)) {
			return Ut.jsHistoryBack("F-6", "이메일을 입력해");

		}

		ResultData joinRd = memberService.join(loginId, loginPw, name, nickname, cellphoneNum, email);

		if (joinRd.isFail()) {
			return Ut.jsHistoryBack(joinRd.getResultCode(), joinRd.getMsg());
		}

		Member member = memberService.getMemberById((int) joinRd.getData1());

		return Ut.jsReplace(joinRd.getResultCode(), joinRd.getMsg(), "../member/login");
	}

	@RequestMapping("/usr/member/myPage")
	public String showmyPage() {
		return "usr/member/myPage";
	}

	@RequestMapping("/usr/member/checkPw")
	public String showCheckPw() {
		return "usr/member/checkPw";
	}

	@RequestMapping("/usr/member/doCheckPw")
	@ResponseBody
	public String doCheckPw(String loginPw) {
		if (Ut.isEmptyOrNull(loginPw)) {
			return Ut.jsHistoryBack("F-1", "비번 써");
		}

		if (rq.getLoginedMember().getLoginPw().equals(Ut.sha256(loginPw)) == false) {
			return Ut.jsHistoryBack("F-2", "비번 틀림");
		}

		return Ut.jsReplace("S-1", Ut.f("비밀번호 확인 성공"), "modify");
	}

	@RequestMapping("/usr/member/modify")
	public String showmyModify() {
		return "usr/member/modify";
	}

	@RequestMapping("/usr/member/doModify")
	@ResponseBody
	public String doModify(HttpServletRequest req, String loginPw, String name, String nickname, String cellphoneNum,
			String email) {

		Rq rq = (Rq) req.getAttribute("rq");

		// 비번은 안바꾸는거 가능(사용자) 비번 null 체크는 x

		if (Ut.isEmptyOrNull(name)) {
			return Ut.jsHistoryBack("F-3", "name 입력 x");
		}
		if (Ut.isEmptyOrNull(nickname)) {
			return Ut.jsHistoryBack("F-4", "nickname 입력 x");
		}
		if (Ut.isEmptyOrNull(cellphoneNum)) {
			return Ut.jsHistoryBack("F-5", "cellphoneNum 입력 x");
		}
		if (Ut.isEmptyOrNull(email)) {
			return Ut.jsHistoryBack("F-6", "email 입력 x");
		}

		ResultData modifyRd;

		if (Ut.isEmptyOrNull(loginPw)) {
			modifyRd = memberService.modifyWithoutPw(rq.getLoginedMemberId(), name, nickname, cellphoneNum, email);
		} else {
			modifyRd = memberService.modify(rq.getLoginedMemberId(), loginPw, name, nickname, cellphoneNum, email);
		}

		return Ut.jsReplace(modifyRd.getResultCode(), modifyRd.getMsg(), "../member/myPage");
	}

	@RequestMapping("/usr/member/getLoginIdDup")
	@ResponseBody
	public ResultData getLoginIdDup(String loginId) {

		if (Ut.isEmpty(loginId)) {
			return ResultData.from("F-1", "아이디를 입력해주세요");
		}

		Member existsMember = memberService.getMemberByLoginId(loginId);

		if (existsMember != null) {
			return ResultData.from("F-2", "해당 아이디는 이미 사용중이야", "loginId", loginId);
		}

		return ResultData.from("S-1", "사용 가능!", "loginId", loginId);
	}

	@RequestMapping("/usr/member/findLoginId")
	public String showFindLoginId() {

		return "usr/member/findLoginId";
	}

	@RequestMapping("/usr/member/doFindLoginId")
	@ResponseBody
	public String doFindLoginId(@RequestParam(defaultValue = "/") String afterFindLoginIdUri, String name,
			String email) {

		Member member = memberService.getMemberByNameAndEmail(name, email);

		if (member == null) {
			return Ut.jsHistoryBack("F-1", "너는 없는 사람이야");
		}

		return Ut.jsReplace("S-1", Ut.f("너의 아이디는 [ %s ] 야", member.getLoginId()), afterFindLoginIdUri);
	}

	@RequestMapping("/usr/member/findLoginPw")
	public String showFindLoginPw() {

		return "usr/member/findLoginPw";
	}

	@RequestMapping("/usr/member/doFindLoginPw")
	@ResponseBody
	public String doFindLoginPw(@RequestParam(defaultValue = "/") String afterFindLoginPwUri, String loginId,
			String email) {

		Member member = memberService.getMemberByLoginId(loginId);

		if (member == null) {
			return Ut.jsHistoryBack("F-1", "너는 없는 사람이야");
		}

		if (member.getEmail().equals(email) == false) {
			return Ut.jsHistoryBack("F-2", "일치하는 이메일이 없는데?");
		}

		ResultData notifyTempLoginPwByEmailRd = memberService.notifyTempLoginPwByEmail(member);

		return Ut.jsReplace(notifyTempLoginPwByEmailRd.getResultCode(), notifyTempLoginPwByEmailRd.getMsg(),
				afterFindLoginPwUri);
	}
}