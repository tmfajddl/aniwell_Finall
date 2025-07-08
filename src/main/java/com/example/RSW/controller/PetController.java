package com.example.RSW.controller;

import com.example.RSW.service.PetAnalysisService;
import com.example.RSW.service.PetService;
import com.example.RSW.service.PetVaccinationService;
import com.example.RSW.vo.Pet;
import com.example.RSW.vo.PetAnalysis;
import com.example.RSW.vo.PetVaccination;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PetController {

    @Autowired
    private PetService petService;

    @Autowired
    private PetVaccinationService petVaccinationService;

    @Autowired
    private PetAnalysisService petAnalysisService;

    @RequestMapping("/usr/pet/list")
    public String showPetList(@RequestParam("memberId") int memberId, Model model) {
        List<Pet> pets = petService.getPetsByMemberId(memberId);
        model.addAttribute("pets", pets);
        return "usr/pet/list"; // JSP or Thymeleaf 페이지
    }

    @RequestMapping("/usr/pet/vaccination")
    public String showPetVaccination(@RequestParam("petId") int petId, Model model) throws Exception {
        List<PetVaccination> list = petVaccinationService.getVaccinationsByPetId(petId);

        List<Map<String, Object>> events = new ArrayList<>();
        for (PetVaccination pv : list) {
            Map<String, Object> injEvent = new HashMap<>();
            injEvent.put("title", pv.getPetName() + " - " + pv.getVaccineName() + " 접종");
            injEvent.put("start", pv.getInjectionDate().toString()); // 날짜를 문자열로
            injEvent.put("color", "#4caf50");

            Map<String, Object> nextEvent = new HashMap<>();
            nextEvent.put("title", pv.getPetName() + " - " + pv.getVaccineName() + " 다음 예정");
            nextEvent.put("start", pv.getNextDueDate().toString());
            nextEvent.put("color", "#f44336");

            events.add(injEvent);
            events.add(nextEvent);
        }

        ObjectMapper mapper = new ObjectMapper();
        String eventsJson = mapper.writeValueAsString(events); // JSON 문자열로 직렬화

        model.addAttribute("eventsJson", eventsJson);
        return "usr/pet/vaccination";
    }

    @RequestMapping("/usr/pet/analysis")
    public String showAnalysis(@RequestParam("petId") int petId, Model model) {
        List<PetAnalysis> analysisList = petAnalysisService.getAnalysisByPetId(petId);
        model.addAttribute("analysisList", analysisList);
        return "usr/pet/analysis"; // JSP 경로
    }
}
