package com.example.RSW.service;

import com.example.RSW.repository.CrewChatMessageRepository;
import com.example.RSW.vo.CrewChatMessage;
import com.example.RSW.vo.CrewMember;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CrewChatMessageService {

    private final CrewChatMessageRepository crewChatMessageRepository;

    public CrewChatMessageService(CrewChatMessageRepository repo) {
        this.crewChatMessageRepository = repo;
    }

    public void saveMessage(CrewChatMessage message) {
        crewChatMessageRepository.insertMessage(message);
    }

    public List<CrewChatMessage> getMessagesByCrewId(int crewId) {
        return crewChatMessageRepository.getMessagesByCrewId(crewId);
    }
}

