package com.example.RSW.controller;

import com.example.RSW.service.QnaService;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.Qna;
import com.example.RSW.vo.ResultData;
import com.example.RSW.vo.Rq;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UsrQnaController {

    @Autowired
    private Rq rq;

    @Autowired
    private QnaService qnaService;

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
        return "usr/qna/detail";
    }

    // 사용자 질문 등록 폼
    @RequestMapping("/usr/qna/ask")
    public String showQnaAskForm() {
        return "usr/qna/ask";
    }

    // 질문 등록 처리
    @PostMapping("/usr/qna/doAsk")
    @ResponseBody
    public ResultData doAsk(HttpServletRequest req, String title, String body, boolean isSecret) {
        Rq rq = (Rq) req.getAttribute("rq");

        if (Ut.isEmptyOrNull(title) || Ut.isEmptyOrNull(body)) {
            return ResultData.from("F-1", "제목과 내용을 입력해주세요.");
        }

        qnaService.writeUserQna(rq.getLoginedMemberId(), title, body, isSecret);

        return ResultData.from("S-1", "질문이 등록되었습니다.");
    }


}