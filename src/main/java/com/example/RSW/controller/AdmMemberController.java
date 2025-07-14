package com.example.RSW.controller;

import com.example.RSW.service.MemberService;
import com.example.RSW.service.NotificationService;
import com.example.RSW.service.VetCertificateService;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Rq;
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

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private Rq rq;

    @RequestMapping("/list")
    public String showMemberList(@RequestParam(defaultValue = "") String searchType,
                                 @RequestParam(defaultValue = "") String searchKeyword,
                                 Model model) {
        List<Member> members = memberService.getForPrintMembers(searchType, searchKeyword);
        model.addAttribute("members", members);
        return "adm/member/list";
    }


    @PostMapping("/changeVetCertStatus")
    @ResponseBody
    public ResultData<?> changeVetCertStatus(@RequestParam int memberId, @RequestParam int approved) {
        vetCertificateService.updateApprovalStatusByMemberId(memberId, approved);

        if (approved == 1) {
            memberService.updateAuthLevel(memberId, 3);
        } else if (approved == 2) {
            memberService.updateAuthLevel(memberId, 1);
        }

        if (rq.getLoginedMemberId() == memberId) {
            Member updatedMember = memberService.getMemberById(memberId);
            rq.login(updatedMember);
        }

        String title = (approved == 1) ? "수의사 인증이 승인되었습니다." : "수의사 인증이 거절되었습니다.";
        String type = (approved == 1) ? "VET_APPROVED" : "VET_REJECTED";
        String link = "/usr/member/myPage";

        int adminId = rq.getLoginedMemberId();
        notificationService.addNotification(memberId, adminId, type, title, link);

        return ResultData.from("S-1", title);
    }

    @PostMapping("/changeAdminStatus")
    @ResponseBody
    public ResultData<?> changeAdminStatus(@RequestParam int memberId, @RequestParam boolean promote) {
        int newLevel = promote ? 7 : 1;
        memberService.updateAuthLevel(memberId, newLevel);

        String msg = promote ? "✅ 관리자 권한이 부여되었습니다." : "❌ 관리자 권한이 해제되었습니다.";

        // 세션 갱신 (본인일 경우)
        if (rq.getLoginedMemberId() == memberId) {
            Member updated = memberService.getMemberById(memberId);
            rq.login(updated);
        }

        return ResultData.from("S-1", msg);
    }


}