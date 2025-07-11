package com.example.RSW.controller;

import com.example.RSW.service.QnaService;
import com.example.RSW.service.VetAnswerService;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.Qna;
import com.example.RSW.vo.Rq;
import com.example.RSW.vo.VetAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/usr/vetAnswer")
public class UsrVetAnswerController {

    @Autowired
    private VetAnswerService vetAnswerService;

    @Autowired
    private QnaService qnaService;

    @Autowired
    private Rq rq;

    @PostMapping("/doWrite")
    @ResponseBody
    public String doWrite(@RequestParam("qnaId") int qnaId,
                          @RequestParam("answer") String answer) {

        Member loginedMember = rq.getLoginedMember();

        // 1. 로그인 여부 및 수의사 권한 체크
        if (loginedMember == null || loginedMember.getAuthLevel() != 3) {
            return Ut.jsHistoryBack("F-1", "수의사만 답변할 수 있습니다.");
        }

        // 2. 질문 존재 여부 확인
        Qna qna = qnaService.getQnaById(qnaId);
        if (qna == null) {
            return Ut.jsHistoryBack("F-2", "해당 질문이 존재하지 않습니다.");
        }

        // 3. 이미 답변이 달렸는지 확인
        if (qna.isAnswered()) {
            return Ut.jsHistoryBack("F-3", "이미 답변이 등록된 질문입니다.");
        }

        // 4. 답변 저장
        vetAnswerService.write(qnaId, loginedMember.getId(), answer, loginedMember.getNickname());

        // 5. QnA isAnswered = true로 업데이트
        qnaService.markAsAnswered(qnaId);

        // 6. 성공 메시지 및 리다이렉트
        return Ut.jsReplace("S-1", "답변이 등록되었습니다.", "/usr/qna/detail?id=" + qnaId);
    }

    // 답변 수정 폼 (필요시)
    @GetMapping("/modify")
    public String showModifyForm(@RequestParam int id, Model model) {
        VetAnswer vetAnswer = vetAnswerService.getById(id);
        Member loginedMember = rq.getLoginedMember();

        if (vetAnswer == null) {
            return rq.historyBackOnView("존재하지 않는 답변입니다.");
        }
        if (loginedMember == null || loginedMember.getId() != vetAnswer.getMemberId()) {
            return rq.historyBackOnView("권한이 없습니다.");
        }

        model.addAttribute("vetAnswer", vetAnswer);
        return "usr/vetAnswer/modify"; // 수정 폼 JSP 경로
    }

    // 답변 수정 처리
    @PostMapping("/doModify")
    @ResponseBody
    public String doModify(@RequestParam int id,
                           @RequestParam String answer) {

        VetAnswer vetAnswer = vetAnswerService.getById(id);
        Member loginedMember = rq.getLoginedMember();

        if (vetAnswer == null) {
            return Ut.jsHistoryBack("F-1", "존재하지 않는 답변입니다.");
        }
        if (loginedMember == null || loginedMember.getId() != vetAnswer.getMemberId()) {
            return Ut.jsHistoryBack("F-2", "권한이 없습니다.");
        }

        vetAnswerService.modify(id, answer);

        return Ut.jsReplace("S-1", "답변이 수정되었습니다.", "/usr/qna/detail?id=" + vetAnswer.getQnaId());
    }

    // 답변 삭제 처리
    @PostMapping("/doDelete")
    @ResponseBody
    public String doDelete(@RequestParam int id) {
        VetAnswer vetAnswer = vetAnswerService.getById(id);
        Member loginedMember = rq.getLoginedMember();

        if (vetAnswer == null) {
            return Ut.jsHistoryBack("F-1", "존재하지 않는 답변입니다.");
        }
        if (loginedMember == null || loginedMember.getId() != vetAnswer.getMemberId()) {
            return Ut.jsHistoryBack("F-2", "권한이 없습니다.");
        }

        vetAnswerService.delete(id);
        // 삭제 후 관련 질문 답변 상태 false로 변경 가능 (필요 시)
        qnaService.markAsAnsweredFalse(vetAnswer.getQnaId());

        return Ut.jsReplace("S-1", "답변이 삭제되었습니다.", "/usr/qna/detail?id=" + vetAnswer.getQnaId());
    }

    @RequestMapping("/vetList")
    public String showVetQnaList(Model model) {
        Member loginedMember = rq.getLoginedMember();

        if (loginedMember == null || loginedMember.getAuthLevel() != 3) {
            return "redirect:/usr/member/login";
        }

        List<Qna> questions = qnaService.getAllQuestions();
        model.addAttribute("questions", questions);
        model.addAttribute("rq", rq);

        return "usr/vetAnswer/vetList"; // 수의사 전용 질문 리스트 페이지
    }


}
