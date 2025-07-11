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
@RequestMapping("/adm/member")
public class AdmMemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private VetCertificateService vetCertificateService;

    @RequestMapping("/list")
    public String showMemberList(@RequestParam(defaultValue = "") String searchType,
                                 @RequestParam(defaultValue = "") String searchKeyword,
                                 Model model) {
        List<Member> members = memberService.getForPrintMembers(searchType, searchKeyword);
        model.addAttribute("members", members);
        return "adm/member/list";
    }


    @PostMapping("/changeAuth")
    @ResponseBody
    public String changeAuth(@RequestParam int id, @RequestParam int authLevel) {
        memberService.updateAuthLevel(id, authLevel);

        // 수의사로 바뀌는 경우 인증서 승인 처리
        if (authLevel == 3) {
            vetCertificateService.updateApprovalStatusByMemberId(id, 1); // 승인
        }

        return Ut.jsReplace("S-1", "권한이 변경되었습니다.", "/adm/member/list");
    }

}

