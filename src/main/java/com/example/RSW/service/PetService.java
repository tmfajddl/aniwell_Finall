package com.example.RSW.service;

import com.example.RSW.repository.PetRepository;
import com.example.RSW.vo.Pet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetService {

    @Autowired
    private PetRepository petRepository;

    public List<Pet> getPetsByMemberId(int memberId) {
        return petRepository.getPetsByMemberId(memberId);
    }
}
