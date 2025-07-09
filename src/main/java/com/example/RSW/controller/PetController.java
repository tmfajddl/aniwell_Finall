package com.example.RSW.controller;

import com.example.RSW.service.PetAnalysisService;
import com.example.RSW.service.PetService;
import com.example.RSW.service.PetVaccinationService;
import com.example.RSW.util.Ut;
import com.example.RSW.vo.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PetController {

    @Autowired
    Rq rq;

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

    @RequestMapping("/usr/pet/join")
    public String showJoin(HttpServletRequest req) {
        return "/usr/pet/join";
    }

    @RequestMapping("/usr/pet/doJoin")
    @ResponseBody
    public String doJoin(HttpServletRequest req, String name, String species, String breed,
                         String gender, String birthDate, double weight) {

        if (Ut.isEmptyOrNull(name)) {
            return Ut.jsHistoryBack("F-1", "이름을 입력하세요");
        }
        if (Ut.isEmptyOrNull(species)) {
            return Ut.jsHistoryBack("F-2", "종을 입력하세요");

        }
        if (Ut.isEmptyOrNull(breed)) {
            return Ut.jsHistoryBack("F-3", "중성화여부를 입력하세요");

        }
        if (Ut.isEmptyOrNull(gender)) {
            return Ut.jsHistoryBack("F-4", "성별을 입력하세요");

        }
        if (Ut.isEmptyOrNull(birthDate)) {
            return Ut.jsHistoryBack("F-5", "생일을 입력하세요");

        }
        if (Ut.isEmptyOrNull(String.valueOf(weight))) {
            return Ut.jsHistoryBack("F-6", "몸무게를 입력하세요");

        }

        ResultData joinRd = petService.insertPet(rq.getLoginedMemberId(),name,species,breed,gender,birthDate,weight);

        int id =rq.getLoginedMemberId();
        return Ut.jsReplace(joinRd.getResultCode(), joinRd.getMsg(), "../pet/list?memberId="+id);
    }

    @RequestMapping("/usr/pet/modify")
    public String showModify(@RequestParam("petId") int petId, Model model) {
        Pet pet = petService.getPetsById(petId);

        model.addAttribute("pet", pet);
        return "usr/pet/modify";
    }

    @RequestMapping("/usr/pet/doModify")
    @ResponseBody
    public String doModify(HttpServletRequest req, @RequestParam("petId") int petId, String name, String species, String breed,
                           String gender, String birthDate, double weight, String photo) {

        // 비번은 안바꾸는거 가능(사용자) 비번 null 체크는 x

        if (Ut.isEmptyOrNull(name)) {
            return Ut.jsHistoryBack("F-1", "이름을 입력하세요");
        }
        if (Ut.isEmptyOrNull(species)) {
            return Ut.jsHistoryBack("F-2", "종을 입력하세요");

        }
        if (Ut.isEmptyOrNull(breed)) {
            return Ut.jsHistoryBack("F-3", "중성화여부를 입력하세요");

        }
        if (Ut.isEmptyOrNull(gender)) {
            return Ut.jsHistoryBack("F-4", "성별을 입력하세요");

        }
        if (Ut.isEmptyOrNull(birthDate)) {
            return Ut.jsHistoryBack("F-5", "생일을 입력하세요");

        }
        if (Ut.isEmptyOrNull(String.valueOf(weight))) {
            return Ut.jsHistoryBack("F-6", "몸무게를 입력하세요");

        }

        ResultData modifyRd;

        if (Ut.isEmptyOrNull(photo)) {
            modifyRd = petService.updatePetyWithoutPhoto(petId, name,species,breed,gender,birthDate,weight);
        } else {
            modifyRd = petService.updatePet(petId, name,species,breed,gender,birthDate,weight,photo);
        }

        int id = rq.getLoginedMemberId();

        return Ut.jsReplace(modifyRd.getResultCode(), modifyRd.getMsg(), "../pet/list?memberId="+id);
    }

    @RequestMapping("/usr/pet/vaccination")
    public String showPetVaccination(@RequestParam("petId") int petId, Model model) throws Exception {
        List<PetVaccination> list = petVaccinationService.getVaccinationsByPetId(petId);

        List<Map<String, Object>> events = new ArrayList<>();
        for (PetVaccination pv : list) {
            // ✅ 접종 이벤트
            Map<String, Object> injEvent = new HashMap<>();
            injEvent.put("id", pv.getId());  // ← 반드시 추가
            injEvent.put("title", pv.getPetName() + " - " + pv.getVaccineName() + " 접종");
            injEvent.put("start", pv.getInjectionDate().toString());
            injEvent.put("color", "#4caf50");

            // ✅ 다음 예정 이벤트
            Map<String, Object> nextEvent = new HashMap<>();
            nextEvent.put("id", pv.getId());  // ← 반드시 추가
            nextEvent.put("title", pv.getPetName() + " - " + pv.getVaccineName() + " 다음 예정");
            nextEvent.put("start", pv.getNextDueDate().toString());
            nextEvent.put("color", "#f44336");

            events.add(injEvent);
            events.add(nextEvent);
        }

        // ✅ JSON으로 변환하여 JSP에 전달
        ObjectMapper objectMapper = new ObjectMapper();
        String eventsJson = objectMapper.writeValueAsString(events);
        model.addAttribute("eventsJson", eventsJson);

        return "usr/pet/vaccination";  // 👉 JSP 파일 경로
    }


    @RequestMapping("/usr/pet/analysis")
    public String showAnalysis(@RequestParam("petId") int petId, Model model) {
        List<PetAnalysis> analysisList = petAnalysisService.getAnalysisByPetId(petId);
        model.addAttribute("analysisList", analysisList);
        return "usr/pet/analysis"; // JSP 경로
    }

    @RequestMapping("/usr/pet/delete")
    public String doDelete(HttpServletRequest req, @RequestParam("petId") int petId) {

        ResultData deleteRd = petService.deletePet(petId);
        int id = rq.getLoginedMemberId();
        return Ut.jsReplace(deleteRd.getResultCode(), deleteRd.getMsg(), "../pet/list?memberId="+id); // JSP 경로
    }

    @RequestMapping("/usr/pet/vaccination/registration")
    public String showRegistration(HttpServletRequest req,@RequestParam("petId") int petId) {
        return "/usr/pet/vaccinationRegistration";
    }

    @RequestMapping("/usr/pet/vaccination/doRegistration")
    @ResponseBody
    public String doRegistration(HttpServletRequest req, @RequestParam("petId") int petId, String vaccineName, String injectionDate) {

        int id = petId;
        if (Ut.isEmptyOrNull(String.valueOf(petId))) {
            return Ut.jsHistoryBack("F-1", "애완동물을 선택하세요");
        }
        if (Ut.isEmptyOrNull(vaccineName)) {
            return Ut.jsHistoryBack("F-2", "백신 이름을 입력하세요");

        }
        if (Ut.isEmptyOrNull(injectionDate)) {
            return Ut.jsHistoryBack("F-3", "접종날짜를 입력하세요");

        }

        ResultData registrationRd = petVaccinationService.insertPetVaccination(petId,vaccineName,injectionDate);
        return Ut.jsReplace(registrationRd.getResultCode(), registrationRd.getMsg(), "../vaccination?petId="+id);
    }

    @RequestMapping("/usr/pet/vaccination/modify")
    public String showVaccinationModify(@RequestParam("vaccinationId") int vaccinationId, Model model) {
        PetVaccination petVaccination = petVaccinationService.getVaccinationsById(vaccinationId);
        model.addAttribute("petVaccination", petVaccination);
        return "usr/pet/vaccinationModify";
    }

    @RequestMapping("/usr/pet/vaccination/doModify")
    @ResponseBody
    public String doVaccinationModify(@RequestParam("vaccinationId") int vaccinationId, String vaccineName, String injectionDate) {


        // 비번은 안바꾸는거 가능(사용자) 비번 null 체크는 x

        if (Ut.isEmptyOrNull(vaccineName)) {
            return Ut.jsHistoryBack("F-1", "백신명을 입력하세요");
        }
        if (Ut.isEmptyOrNull(injectionDate)) {
            return Ut.jsHistoryBack("F-2", "접종일자를 입력하세요");

        }

        ResultData modifyRd = petVaccinationService.updatePetVaccination( vaccinationId, vaccineName,injectionDate);
        int id = petVaccinationService.getPetIdById(vaccinationId);
        return Ut.jsReplace(modifyRd.getResultCode(), modifyRd.getMsg(), "../vaccination?petId="+id);
    }

    @RequestMapping("/usr/pet/vaccination/detail")
    public String showVaccinationDetail(@RequestParam("vaccinationId") int vaccinationId, Model model) {
        PetVaccination petVaccination = petVaccinationService.getVaccinationsById(vaccinationId);
        model.addAttribute("petVaccination", petVaccination);
        return "usr/pet/vaccinationDetail";  // 상세보기 JSP 페이지
    }

    @RequestMapping("/usr/pet/vaccination/delete")
    public String doVaccinationDelete(@RequestParam("vaccinationId") int  vaccinationId) {
        int id = petVaccinationService.getPetIdById(vaccinationId);
        ResultData deleteRd = petVaccinationService.deletePetVaccination(vaccinationId);
        return Ut.jsReplace(deleteRd.getResultCode(), deleteRd.getMsg(), "../vaccination?petId="+id); // JSP 경로
    }
}
