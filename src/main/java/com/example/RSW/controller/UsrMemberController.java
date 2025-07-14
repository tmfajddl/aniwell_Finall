package com.example.RSW.controller;

import com.example.RSW.service.VetCertificateService;
import com.example.RSW.vo.VetCertificate;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.RSW.vo.Rq;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.util.Ut;
import com.example.RSW.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class UsrMemberController {

    @Autowired
    private Rq rq;

    @Autowired
    private MemberService memberService;

    @Autowired
    private VetCertificateService vetCertificateService;

    // 로그아웃 처리
    @RequestMapping("/usr/member/doLogout")
    @ResponseBody
    public String doLogin(HttpServletRequest req) {
        Rq rq = (Rq) req.getAttribute("rq");
        rq.logout(); // 세션 제거
        return Ut.jsReplace("S-1", "로그아웃 성공", "/");
    }

    // 로그인 폼
    @RequestMapping("/usr/member/login")
    public String showLogin(HttpServletRequest req) {
        return "/usr/member/login";
    }

    // 로그인 처리
    @RequestMapping("/usr/member/doLogin")
    @ResponseBody
    public String doLogin(HttpServletRequest req, HttpServletResponse resp, String loginId, String loginPw,
                          @RequestParam(defaultValue = "/") String afterLoginUri) {

        // rq 객체 세션에서 조회
        Rq rq = (Rq) req.getSession().getAttribute("rq");
        if (rq == null) {
            rq = new Rq(req, resp, memberService);
            req.getSession().setAttribute("rq", rq);
        }

        // 입력값 체크
        if (Ut.isEmptyOrNull(loginId)) return Ut.jsHistoryBack("F-1", "아이디를 입력해");
        if (Ut.isEmptyOrNull(loginPw)) return Ut.jsHistoryBack("F-2", "비밀번호를 입력해");

        Member member = memberService.getMemberByLoginId(loginId);
        if (member == null) return Ut.jsHistoryBack("F-3", Ut.f("%s는(은) 없는 아이디야", loginId));
        if (!member.getLoginPw().equals(Ut.sha256(loginPw))) return Ut.jsHistoryBack("F-4", "비밀번호가 일치하지 않습니다!!!!!");
        if (member.isDelStatus()) return Ut.jsHistoryBack("F-5", "탈퇴한 회원입니다.");

        rq.login(member); // 로그인 처리
        req.getSession().setAttribute("rq", rq); // 세션 갱신

        return Ut.jsReplace("S-1", Ut.f("%s님 환영합니다", member.getNickname()), afterLoginUri);
    }

    // 회원가입 폼
    @RequestMapping("/usr/member/join")
    public String showJoin(HttpServletRequest req) {
        return "/usr/member/join";
    }

    // 회원가입 처리
    @RequestMapping("/usr/member/doJoin")
    @ResponseBody
    public String doJoin(HttpServletRequest req, String loginId, String loginPw, String name, String nickname,
                         String cellphone, String email, String address, String authName,
                         @RequestParam(defaultValue = "1") int authLevel) {

        // 필수 입력값 유효성 검사
        if (Ut.isEmptyOrNull(loginId)) return Ut.jsHistoryBack("F-1", "아이디를 입력해");
        if (Ut.isEmptyOrNull(loginPw)) return Ut.jsHistoryBack("F-2", "비밀번호를 입력해");
        if (Ut.isEmptyOrNull(name)) return Ut.jsHistoryBack("F-3", "이름을 입력해");
        if (Ut.isEmptyOrNull(nickname)) return Ut.jsHistoryBack("F-4", "닉네임을 입력해");
        if (Ut.isEmptyOrNull(cellphone)) return Ut.jsHistoryBack("F-5", "전화번호를 입력해");
        if (Ut.isEmptyOrNull(email)) return Ut.jsHistoryBack("F-6", "이메일을 입력해");
        if (Ut.isEmptyOrNull(address)) return Ut.jsHistoryBack("F-7", "주소를 입력해");
        if (Ut.isEmptyOrNull(authName)) return Ut.jsHistoryBack("F-8", "인증명을 입력해");

        String hashedLoginPw = Ut.sha256(loginPw); // 비밀번호 해시화
        ResultData joinRd = memberService.join(loginId, hashedLoginPw, name, nickname, cellphone, email, address, authName, authLevel);

        if (joinRd.isFail()) return Ut.jsHistoryBack(joinRd.getResultCode(), joinRd.getMsg());

        return Ut.jsReplace(joinRd.getResultCode(), joinRd.getMsg(), "../member/login");
    }

    // 마이페이지
    @RequestMapping({"/usr/member/myPage", "/usr/member/mypage"})
    public String showMyPage(HttpServletRequest req, Model model) {
        Rq rq = (Rq) req.getAttribute("rq");
        model.addAttribute("member", rq.getLoginedMember());
        return "usr/member/myPage";
    }

    // 비밀번호 확인 폼
    @RequestMapping("/usr/member/checkPw")
    public String showCheckPw() {
        return "usr/member/checkPw";
    }

    // 비밀번호 확인 처리
    @RequestMapping("/usr/member/doCheckPw")
    @ResponseBody
    public String doCheckPw(String loginPw) {
        if (Ut.isEmptyOrNull(loginPw)) return Ut.jsHistoryBack("F-1", "비번 써");
        if (!rq.getLoginedMember().getLoginPw().equals(Ut.sha256(loginPw))) return Ut.jsHistoryBack("F-2", "비번 틀림");

        return Ut.jsReplace("S-1", "비밀번호 확인 성공", "modify");
    }

    // 회원정보 수정 폼
    @RequestMapping("/usr/member/modify")
    public String showmyModify() {
        return "usr/member/modify";
    }

    // 회원정보 수정 처리
    @RequestMapping("/usr/member/doModify")
    @ResponseBody
    public String doModify(HttpServletRequest req, String loginPw, String name, String nickname, String cellphone, String email) {
        Rq rq = (Rq) req.getAttribute("rq");

        if (Ut.isEmptyOrNull(name)) return Ut.jsHistoryBack("F-3", "name 입력 x");
        if (Ut.isEmptyOrNull(nickname)) return Ut.jsHistoryBack("F-4", "nickname 입력 x");
        if (Ut.isEmptyOrNull(cellphone)) return Ut.jsHistoryBack("F-5", "cellphone 입력 x");
        if (Ut.isEmptyOrNull(email)) return Ut.jsHistoryBack("F-6", "email 입력 x");

        ResultData modifyRd = Ut.isEmptyOrNull(loginPw)
                ? memberService.modifyWithoutPw(rq.getLoginedMemberId(), name, nickname, cellphone, email)
                : memberService.modify(rq.getLoginedMemberId(), loginPw, name, nickname, cellphone, email);

        return Ut.jsReplace(modifyRd.getResultCode(), modifyRd.getMsg(), "../member/myPage");
    }

    // 아이디 중복 확인 (AJAX)
    @RequestMapping("/usr/member/getLoginIdDup")
    @ResponseBody
    public ResultData getLoginIdDup(String loginId) {
        if (Ut.isEmpty(loginId)) return ResultData.from("F-1", "아이디를 입력해주세요");

        Member existsMember = memberService.getMemberByLoginId(loginId);
        if (existsMember != null) return ResultData.from("F-2", "해당 아이디는 이미 사용중이야", "loginId", loginId);

        return ResultData.from("S-1", "사용 가능!", "loginId", loginId);
    }

    // 아이디 찾기 폼
    @RequestMapping("/usr/member/findLoginId")
    public String showFindLoginId() {
        return "usr/member/findLoginId";
    }

    // 아이디 찾기 처리
    @RequestMapping("/usr/member/doFindLoginId")
    @ResponseBody
    public String doFindLoginId(@RequestParam(defaultValue = "/usr/member/login") String afterFindLoginIdUri,
                                String name, String email) {

        Member member = memberService.getMemberByNameAndEmail(name, email);
        if (member == null) return Ut.jsHistoryBack("F-1", "너는 없는 사람이야");

        return Ut.jsReplace("S-1", Ut.f("너의 아이디는 [ %s ] 야", member.getLoginId()), afterFindLoginIdUri);
    }

    // 비밀번호 찾기 폼
    @RequestMapping("/usr/member/findLoginPw")
    public String showFindLoginPw() {
        return "usr/member/findLoginPw";
    }

    // 비밀번호 찾기 처리
    @RequestMapping("/usr/member/doFindLoginPw")
    @ResponseBody
    public String doFindLoginPw(@RequestParam(defaultValue = "/") String afterFindLoginPwUri, String loginId,
                                String email) {

        Member member = memberService.getMemberByLoginId(loginId);
        if (member == null) return Ut.jsHistoryBack("F-1", "너는 없는 사람이야");
        if (!member.getEmail().equals(email)) return Ut.jsHistoryBack("F-2", "일치하는 이메일이 없는데?");

        ResultData notifyTempLoginPwByEmailRd = memberService.notifyTempLoginPwByEmail(member);
        return Ut.jsReplace(notifyTempLoginPwByEmailRd.getResultCode(), notifyTempLoginPwByEmailRd.getMsg(), afterFindLoginPwUri);
    }

    // 회원 탈퇴
    @RequestMapping("/usr/member/doWithdraw")
    @ResponseBody
    public String doWithdraw(HttpServletRequest req) {
        Rq rq = (Rq) req.getAttribute("rq");

        if (!rq.isLogined()) return Ut.jsHistoryBack("F-1", "로그인 후 이용해주세요.");

        memberService.withdrawMember(rq.getLoginedMemberId());
        rq.logout();

        return Ut.jsReplace("S-1", "회원 탈퇴가 완료되었습니다.", "/");
    }

    // 수의사 인증서 제출 폼
    @RequestMapping("/usr/member/vetCert")
    public String showVetCertForm(HttpServletRequest req, Model model) {
        Rq rq = (Rq) req.getAttribute("rq");

        if (!"수의사".equals(rq.getLoginedMember().getAuthName())) {
            model.addAttribute("errorMsg", "수의사만 인증서 제출이 가능합니다.");
            return "common/error";
        }

        return "usr/member/vetCertUpload";
    }

    // 수의사 인증서 업로드 처리
    @RequestMapping("/usr/member/doVetCertUpload")
    @ResponseBody
    public String doVetCertUpload(HttpServletRequest req, @RequestParam("file") MultipartFile file) {
        Rq rq = (Rq) req.getAttribute("rq");

        if (file.isEmpty()) return Ut.jsHistoryBack("F-1", "파일을 선택해주세요.");

        try {
            String originalFilename = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String savedFileName = uuid + "_" + originalFilename;
            String uploadDir = "C:/upload/vet_certificates";

            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            file.transferTo(new File(uploadDir + "/" + savedFileName));

            VetCertificate cert = new VetCertificate();
            cert.setMemberId(rq.getLoginedMemberId());
            cert.setFileName(originalFilename);
            cert.setFilePath(savedFileName);
            cert.setUploadedAt(LocalDateTime.now());
            cert.setApproved(0); // 대기 상태

            vetCertificateService.registerCertificate(cert);
            return Ut.jsReplace("S-1", "수의사 인증서가 등록되었습니다. 관리자 승인을 기다려주세요.", "myCert");

        } catch (Exception e) {
            return Ut.jsHistoryBack("F-2", "파일 업로드 중 오류가 발생했습니다.");
        }
    }

    // 나의 인증서 보기
    @RequestMapping("/usr/member/myCert")
    public String showMyCertificate(HttpServletRequest req, Model model) {
        Rq rq = (Rq) req.getAttribute("rq");
        VetCertificate cert = vetCertificateService.getCertificateByMemberId(rq.getLoginedMemberId());
        model.addAttribute("cert", cert);
        return "usr/member/myCert";
    }

    // 인증서 삭제 처리
    @RequestMapping("/usr/member/deleteVetCert")
    @ResponseBody
    public String deleteVetCert(HttpServletRequest req) {
        Rq rq = (Rq) req.getAttribute("rq");
        VetCertificate cert = vetCertificateService.getCertificateByMemberId(rq.getLoginedMemberId());

        if (cert == null) return Ut.jsHistoryBack("F-1", "삭제할 인증서가 없습니다.");

        vetCertificateService.deleteCertificateWithFile(cert);
        return Ut.jsReplace("S-1", "인증서가 삭제되었습니다.", "/usr/member/vetCert");
    }
}
