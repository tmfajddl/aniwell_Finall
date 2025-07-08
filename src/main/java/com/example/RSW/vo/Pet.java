package com.example.RSW.vo;

import lombok.Data;

import java.security.Timestamp;
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
    private String photo;
    private Timestamp createdAt;
    private Timestamp updatedAt;

}

