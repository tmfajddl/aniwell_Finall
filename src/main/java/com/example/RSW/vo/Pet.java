package com.example.RSW.vo;

import lombok.Data;
import java.util.Date;

@Data
public class Pet {
    private int id;
    private int memberId;
    private String name;
    private String species;
    private String breed;
    private String gender;
    private Date birthDate;
    private double weight;
}

