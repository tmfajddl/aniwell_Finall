package com.example.RSW.controller;

import com.example.RSW.service.MemberService;
import com.example.RSW.service.VetCertificateService;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/adm/member") // 관리자용 회원 관리 컨트롤러
public class AdmMemberController {

    @Autowired
    private MemberService memberService; // 회원 관련 서비스 의존성 주입

    @Autowired
    private VetCertificateService vetCertificateService; // 수의사 인증서 관련 서비스 의존성 주입

    // 회원 목록 페이지 요청 처리
    @RequestMapping("/list")
    public String showMemberList(@RequestParam(defaultValue = "") String searchType, // 검색 조건 타입 (ex. 이름, 아이디 등)
                                 @RequestParam(defaultValue = "") String searchKeyword, // 검색 키워드
                                 Model model) {
        // 조건에 맞는 회원 리스트 조회
        List<Member> members = memberService.getForPrintMembers(searchType, searchKeyword);

        // 모델에 회원 리스트 추가
        model.addAttribute("members", members);

        // 관리자 회원 목록 페이지로 이동
        return "adm/member/list";
    }

    // 회원 권한 변경 요청 처리 (AJAX)
    @PostMapping("/changeAuth")
    @ResponseBody
    public String changeAuth(@RequestParam int id, @RequestParam int authLevel) {
        // 회원 권한 레벨 수정
        memberService.updateAuthLevel(id, authLevel);

        // 권한이 '수의사(3)'로 변경되는 경우 → 인증서 승인 처리
        if (authLevel == 3) {
            vetCertificateService.updateApprovalStatusByMemberId(id, 1); // 승인 상태로 업데이트
        }

        // 변경 후 관리자 회원 목록 페이지로 이동
        return Ut.jsReplace("S-1", "권한이 변경되었습니다.", "/adm/member/list");
    }

}


