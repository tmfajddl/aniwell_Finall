package com.example.RSW.controller;

import com.example.RSW.service.QnaService;
import com.example.RSW.service.VetAnswerService;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
public class UsrQnaController {

    @Autowired
    private Rq rq;

    @Autowired
    private QnaService qnaService;

    @Autowired
    private VetAnswerService vetAnswerService;

    // 질문 리스트 페이지
    @RequestMapping("/usr/qna/list")
    public String showQnaList(Model model,
                              @RequestParam(value = "selectedId", required = false) Integer selectedId) {
        List<Qna> qnas = qnaService.getPublicFaqList(); // 자주 묻는 질문
        List<Qna> myQnas = qnaService.getUserQnaByMemberId(rq.getLoginedMemberId());

        Qna selectedQna = null;
        if (selectedId != null) {
            selectedQna = qnaService.getQnaById(selectedId);
        }

        model.addAttribute("qnas", qnas);
        model.addAttribute("myQnas", myQnas);
        model.addAttribute("selectedQna", selectedQna);
        return "usr/qna/list";
    }


    // 질문 상세 보기
    @RequestMapping("/usr/qna/detail")
    public String showQnaDetail(Model model, int id) {
        Qna qna = qnaService.getQnaById(id);
        model.addAttribute("qna", qna);

        // 수의사 답변 리스트 조회
        List<VetAnswer> vetAnswers = vetAnswerService.getByQnaId(id);
        model.addAttribute("vetAnswers", vetAnswers);

        model.addAttribute("rq", rq);
        return "usr/qna/detail";
    }

    // 사용자 질문 등록 폼
    @RequestMapping("/usr/qna/ask")
    public String showQnaAskForm() {
        return "usr/qna/ask";
    }

    // 질문 등록 처리
    @RequestMapping(value = "/usr/qna/doAsk", method = RequestMethod.POST)
    @ResponseBody
    public ResultData doAsk(HttpServletRequest req,
                            @RequestParam String title,
                            @RequestParam String body,
                            @RequestParam(required = false, defaultValue = "false") boolean isSecret) {

        Rq rq = (Rq) req.getAttribute("rq");
        int loginedMemberId = rq.getLoginedMemberId();

        if (Ut.isEmptyOrNull(title)) {
            return ResultData.from("F-1", "제목을 입력해주세요.");
        }

        if (Ut.isEmptyOrNull(body)) {
            return ResultData.from("F-2", "내용을 입력해주세요.");
        }

        qnaService.writeUserQna(loginedMemberId, title, body, isSecret);

        return ResultData.from("S-1", "질문이 성공적으로 등록되었습니다.");
    }

    @RequestMapping("/usr/qna/doDelete")
    @ResponseBody
    public void doDelete(@RequestParam int id) throws IOException {
        Qna qna = qnaService.getQnaById(id);

        if (qna == null || !qna.isActive()) {
            rq.printHistoryBack("존재하지 않는 질문입니다.");
            return;
        }

        if (qna.getMemberId() != rq.getLoginedMemberId()) {
            rq.printHistoryBack("권한이 없습니다.");
            return;
        }

        qnaService.deleteQna(id);
        rq.printReplace("S-1", "질문이 삭제되었습니다.", "/usr/qna/list");
    }

    @RequestMapping("/usr/qna/modify")
    public String showModifyForm(@RequestParam int id, Model model) {
        Qna qna = qnaService.getQnaById(id);

        if (qna == null || !qna.isActive()) {
            return rq.historyBackOnView("존재하지 않는 질문입니다.");
        }

        if (qna.getMemberId() != rq.getLoginedMemberId()) {
            return rq.historyBackOnView("권한이 없습니다.");
        }

        model.addAttribute("qna", qna);
        return "usr/qna/modify"; // 이게 바로 modify.jsp와 연결됨
    }

    @RequestMapping(value = "/usr/qna/doModify", method = RequestMethod.POST)
    @ResponseBody
    public String doModify(@RequestParam int id,
                           @RequestParam String title,
                           @RequestParam String body,
                           @RequestParam(defaultValue = "false") boolean isSecret) throws IOException {

        Qna qna = qnaService.getQnaById(id);

        if (qna == null || !qna.isActive()) {
            return rq.historyBackOnView("존재하지 않는 질문입니다.");
        }

        if (qna.getMemberId() != rq.getLoginedMemberId()) {
            return rq.historyBackOnView("권한이 없습니다.");
        }

        qnaService.modifyQna(id, title, body, isSecret);

        rq.printReplace("S-1", "질문이 수정되었습니다.", "/usr/qna/detail?id=" + id);
        return null;
    }


}