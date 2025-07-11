package com.example.RSW.controller;

import com.example.RSW.service.QnaService;
import com.example.RSW.service.VetAnswerService;
import com.example.RSW.vo.Qna;
import com.example.RSW.vo.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/adm/qna")
public class AdmQnaController {

    @Autowired
    private Rq rq;
    @Autowired
    private QnaService qnaService;

    @Autowired
    private VetAnswerService vetAnswerService;

    @GetMapping("/list")
    public String showQnaList(Model model) {
        model.addAttribute("qnaList", qnaService.findAll());
        return "adm/qna/list";
    }

    @GetMapping("/detail")
    public String showQnaDetail(@RequestParam int id, Model model) {
        model.addAttribute("qna", qnaService.findById(id));
        model.addAttribute("answer", vetAnswerService.findByQnaId(id));
        return "adm/qna/detail";
    }

    @PostMapping("/doAnswer")
    public String doAnswer(@RequestParam int qnaId, @RequestParam String answer) {
        String vetName = rq.getLoginedMember().getNickname(); // 또는 이름
        vetAnswerService.write(qnaId, answer, vetName);
        return "redirect:/adm/qna/detail?id=" + qnaId;
    }


    @PostMapping("/doUpdateAnswer")
    public String doUpdateAnswer(@RequestParam int id, @RequestParam String answer) {
        vetAnswerService.update(id, answer);
        return "redirect:/adm/qna/detail?id=" + vetAnswerService.getQnaIdByAnswerId(id);
    }

    @PostMapping("/doDeleteAnswer")
    public String doDeleteAnswer(@RequestParam int id) {
        long qnaId = vetAnswerService.getQnaIdByAnswerId(id);
        vetAnswerService.delete(id);
        return "redirect:/adm/qna/detail?id=" + qnaId;
    }

    @GetMapping("/edit")
    public String showEditForm(@RequestParam int id, Model model) {
        model.addAttribute("qna", qnaService.findById(id));
        return "adm/qna/edit"; // edit.jsp 존재해야 함
    }

    @PostMapping("/doModify")
    public String doModify(@RequestParam int id,
                           @RequestParam String title,
                           @RequestParam String body) {
        qnaService.modify(id, title, body);
        return "redirect:/adm/qna/detail?id=" + id;
    }

    @PostMapping("/doDelete")
    public String doDelete(@RequestParam int id) {
        System.out.println("삭제 요청 id: " + id); // 로그 확인
        qnaService.delete(id);
        return "redirect:/adm/qna/list";
    }

    @GetMapping("/write")
    public String showWriteForm() {
        return "adm/qna/write";
    }

    @PostMapping("/doWrite")
    public String doWrite(@RequestParam String title, @RequestParam String body) {
        int memberId = rq.getLoginedMemberId(); // 관리자 ID
        qnaService.writeFaq(memberId, title, body); // isFaq = 1로 저장
        return "redirect:/adm/qna/list";
    }


}
