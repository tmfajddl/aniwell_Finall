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
import org.springframework.web.bind.annotation.*;

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



}