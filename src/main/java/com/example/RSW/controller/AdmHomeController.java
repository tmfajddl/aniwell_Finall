package com.example.RSW.controller;

import com.example.RSW.vo.Rq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/adm")
public class AdmHomeController {

    @Autowired
    private Rq rq;

    @GetMapping("/dashboard")
    public String showDashboard() {
        if (!rq.isAdmin()) {
            return rq.historyBackOnView("관리자만 접근 가능합니다.");
        }
        return "adm/main/dashboard";
    }
}
