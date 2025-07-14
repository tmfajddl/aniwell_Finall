package com.example.RSW.arduino;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ArduinoRunner implements CommandLineRunner {
    @Override
    public void run(String... args) {
        SerialReader.start();
    }
}
