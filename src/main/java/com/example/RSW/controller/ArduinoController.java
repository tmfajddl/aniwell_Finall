package com.example.RSW.controller;

import com.example.RSW.arduino.SerialReader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ArduinoController {

    @RequestMapping("/usr/arduino/show")
    public String showData(Model model) {
        model.addAttribute("data", SerialReader.getLatestData());
        return "usr/arduino/show";
    }

    @RequestMapping("/usr/arduino/api/data")
    @ResponseBody
    public String getDataAjax() {
        return SerialReader.getLatestData();
    }
}
