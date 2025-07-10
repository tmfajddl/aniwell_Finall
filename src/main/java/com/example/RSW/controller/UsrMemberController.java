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
    public String doLogin(HttpServletRequest req, HttpServletResponse resp, String loginId, String loginPw,
                          @RequestParam(defaultValue = "/") String afterLoginUri) {

        // 세션에서 rq 객체 가져오기
        Rq rq = (Rq) req.getSession().getAttribute("rq");

        // rq 객체가 없다면 새로운 rq 객체를 생성하여 세션에 저장
        if (rq == null) {
            // 새로운 rq 객체 생성, resp 객체도 전달
            rq = new Rq(req, resp, memberService);
            req.getSession().setAttribute("rq", rq);  // 세션에 rq 객체 저장
        }

        // 로그인 필수 값 체크
        if (Ut.isEmptyOrNull(loginId)) {
            return Ut.jsHistoryBack("F-1", "아이디를 입력해");
        }
        if (Ut.isEmptyOrNull(loginPw)) {
            return Ut.jsHistoryBack("F-2", "비밀번호를 입력해");
        }

        // 로그인 시 회원정보를 가져옵니다.
        Member member = memberService.getMemberByLoginId(loginId);

        // 회원이 없으면 에러 반환
        if (member == null) {
            return Ut.jsHistoryBack("F-3", Ut.f("%s는(은) 없는 아이디야", loginId));
        }

        // 비밀번호 해시값 비교
        if (!member.getLoginPw().equals(Ut.sha256(loginPw))) {
            return Ut.jsHistoryBack("F-4", Ut.f("비밀번호가 일치하지 않습니다!!!!!"));
        }

        if (member.isDelStatus()) {
            return Ut.jsHistoryBack("F-5", "탈퇴한 회원입니다.");
        }

        // 로그인 처리 후 rq 객체에 회원 정보를 설정
        rq.login(member);

        // 로그인 후 rq 객체를 세션에 저장하여 이후 요청에서도 사용
        req.getSession().setAttribute("rq", rq);  // 세션에 rq 객체 저장


        return Ut.jsReplace("S-1", Ut.f("%s님 환영합니다", member.getNickname()), afterLoginUri);
    }


    @RequestMapping("/usr/member/join")
    public String showJoin(HttpServletRequest req) {
        return "/usr/member/join";
    }

    @RequestMapping("/usr/member/doJoin")
    @ResponseBody
    public String doJoin(HttpServletRequest req, String loginId, String loginPw, String name, String nickname,
                         String cellphone, String email, String address, String authName,
                         @RequestParam(defaultValue = "1") int authLevel) {

        // 필수 입력값 체크
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
        if (Ut.isEmptyOrNull(cellphone)) {
            System.out.println("전화번호가 비어있습니다: " + cellphone); // 로그 추가
            return Ut.jsHistoryBack("F-5", "전화번호를 입력해");
        }
        if (Ut.isEmptyOrNull(email)) {
            return Ut.jsHistoryBack("F-6", "이메일을 입력해");
        }
        if (Ut.isEmptyOrNull(address)) {
            return Ut.jsHistoryBack("F-7", "주소를 입력해");
        }
        if (Ut.isEmptyOrNull(authName)) {
            return Ut.jsHistoryBack("F-8", "인증명을 입력해");
        }

        // 비밀번호를 SHA-256으로 해시화
        String hashedLoginPw = Ut.sha256(loginPw);

        // 회원가입 서비스 호출
        ResultData joinRd = memberService.join(loginId, hashedLoginPw, name, nickname, cellphone, email, address, authName, authLevel);

        if (joinRd.isFail()) {
            return Ut.jsHistoryBack(joinRd.getResultCode(), joinRd.getMsg());
        }

        // 성공적으로 가입된 회원 정보를 가져옴
        Member member = memberService.getMemberById((int) joinRd.getData1());

        // 회원가입 성공 메시지
        return Ut.jsReplace(joinRd.getResultCode(), joinRd.getMsg(), "../member/login");
    }


    @RequestMapping("/usr/member/myPage")
    public String showmyPage(HttpServletRequest req, Model model) {
        Rq rq = (Rq) req.getAttribute("rq");
        Member loginedMember = rq.getLoginedMember();

        model.addAttribute("member", loginedMember); // ✅ JSP에 전달

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
    public String doModify(HttpServletRequest req, String loginPw, String name, String nickname, String cellphone,
                           String email) {

        Rq rq = (Rq) req.getAttribute("rq");

        // 비번은 안바꾸는거 가능(사용자) 비번 null 체크는 x

        if (Ut.isEmptyOrNull(name)) {
            return Ut.jsHistoryBack("F-3", "name 입력 x");
        }
        if (Ut.isEmptyOrNull(nickname)) {
            return Ut.jsHistoryBack("F-4", "nickname 입력 x");
        }
        if (Ut.isEmptyOrNull(cellphone)) {
            return Ut.jsHistoryBack("F-5", "cellphone 입력 x");
        }
        if (Ut.isEmptyOrNull(email)) {
            return Ut.jsHistoryBack("F-6", "email 입력 x");
        }

        ResultData modifyRd;

        if (Ut.isEmptyOrNull(loginPw)) {
            modifyRd = memberService.modifyWithoutPw(rq.getLoginedMemberId(), name, nickname, cellphone, email);
        } else {
            modifyRd = memberService.modify(rq.getLoginedMemberId(), loginPw, name, nickname, cellphone, email);
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
    public String doFindLoginId(@RequestParam(defaultValue = "/usr/member/login") String afterFindLoginIdUri,
                                String name, String email) {

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

    @RequestMapping("/usr/member/doWithdraw")
    @ResponseBody
    public String doWithdraw(HttpServletRequest req) {
        Rq rq = (Rq) req.getAttribute("rq");

        if (!rq.isLogined()) {
            return Ut.jsHistoryBack("F-1", "로그인 후 이용해주세요.");
        }

        memberService.withdrawMember(rq.getLoginedMemberId());
        rq.logout(); // 세션 종료

        return Ut.jsReplace("S-1", "회원 탈퇴가 완료되었습니다.", "/");
    }

    @RequestMapping("/usr/member/vetCert")
    public String showVetCertForm(HttpServletRequest req, Model model) {
        Rq rq = (Rq) req.getAttribute("rq");

        // 수의사 신청자인지 확인
        if (!"수의사".equals(rq.getLoginedMember().getAuthName())) {
            model.addAttribute("errorMsg", "수의사만 인증서 제출이 가능합니다.");
            return "common/error";
        }

        return "usr/member/vetCertUpload"; // JSP 경로
    }

    @RequestMapping("/usr/member/doVetCertUpload")
    @ResponseBody
    public String doVetCertUpload(HttpServletRequest req, @RequestParam("file") MultipartFile file) {
        Rq rq = (Rq) req.getAttribute("rq");

        if (file.isEmpty()) {
            return Ut.jsHistoryBack("F-1", "파일을 선택해주세요.");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String savedFileName = uuid + "_" + originalFilename;
            String uploadDir = "C:/upload/vet_certificates";

            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

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

    @RequestMapping("/usr/member/myCert")
    public String showMyCertificate(HttpServletRequest req, Model model) {
        Rq rq = (Rq) req.getAttribute("rq");

        VetCertificate cert = vetCertificateService.getCertificateByMemberId(rq.getLoginedMemberId());

        model.addAttribute("cert", cert);
        return "usr/member/myCert";
    }

    @RequestMapping("/usr/member/deleteVetCert")
    @ResponseBody
    public String deleteVetCert(HttpServletRequest req) {
        Rq rq = (Rq) req.getAttribute("rq");
        VetCertificate cert = vetCertificateService.getCertificateByMemberId(rq.getLoginedMemberId());

        if (cert == null) {
            return Ut.jsHistoryBack("F-1", "삭제할 인증서가 없습니다.");
        }

        vetCertificateService.deleteCertificateWithFile(cert);

        return Ut.jsReplace("S-1", "인증서가 삭제되었습니다.", "/usr/member/vetCert");
    }



}