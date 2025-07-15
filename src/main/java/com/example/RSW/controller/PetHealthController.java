package com.example.RSW.controller;

import com.example.RSW.dto.PetHealthLogDto;
import com.example.RSW.service.PetHealthService;
import com.example.RSW.vo.PetHealthLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class PetHealthController {

        @Autowired
        private PetHealthService healthService;

        // POST: ESP32나 클라이언트에서 데이터 수신
        @RequestMapping("/usr/pet/health/log")
        @ResponseBody
        public ResponseEntity<String> saveLog(@RequestBody PetHealthLogDto dto) {
            healthService.save(dto);
            return ResponseEntity.ok("Health Log Saved");
        }

        // GET: JSP에서 health 로그 조회
        @RequestMapping("/usr/pet/health")
        public String showLogs(@RequestParam("petId") int petId, Model model) {
            List<PetHealthLog> logs = healthService.getLogsByPetId(petId);
            model.addAttribute("logs", logs);
            model.addAttribute("petId", petId);
            return "usr/pet/health";
        }
}
