package com.example.RSW.controller;

import com.example.RSW.service.PetBleActivityService;
import com.example.RSW.dto.PetBleActivityDto;
import com.example.RSW.vo.PetBleActivity;
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
public class BleActivityController {
    @Autowired
    private PetBleActivityService bleService;

    public BleActivityController(PetBleActivityService bleService) {
        this.bleService = bleService;
    }

    @RequestMapping("/usr/pet/activity/save")
    @ResponseBody
    public ResponseEntity<String> saveActivity(@RequestBody PetBleActivityDto dto) {
        bleService.save(dto);
        return ResponseEntity.ok("BLE Activity Saved");
    }

    @RequestMapping("/usr/pet/activity")
    public String showBleActivityList(@RequestParam("petId") int petId, Model model) {
        List<PetBleActivity> activities = bleService.getActivitiesByPetId(petId);
        model.addAttribute("activities", activities);
        model.addAttribute("petId", petId);
        return "usr/pet/activity"; // JSP 경로
    }
}
