package com.example.RSW.controller;

import com.example.RSW.service.CrewChatMessageService;
import com.example.RSW.vo.CrewChatMessage;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class CrewChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CrewChatMessageService chatService;

    public CrewChatController(SimpMessagingTemplate messagingTemplate,
                              CrewChatMessageService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    //채팅 전송 로직
    @MessageMapping("/chat.send/{crewId}")
    public void sendMessage(@DestinationVariable int crewId, CrewChatMessage message) {
        message.setSentAt(LocalDateTime.now());

        // DB에 저장
        chatService.saveMessage(message);

        // 구독자에게 메시지 전달
        messagingTemplate.convertAndSend("/topic/crew/" + crewId, message);
    }
}

