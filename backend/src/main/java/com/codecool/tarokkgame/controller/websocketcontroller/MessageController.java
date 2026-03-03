package com.codecool.tarokkgame.controller.websocketcontroller;

import com.codecool.tarokkgame.exceptionhandling.customexception.NotAllowedOperationException;
import com.codecool.tarokkgame.model.dto.messagedto.JoinMessageDTO;
import com.codecool.tarokkgame.model.dto.messagedto.JoinRequestDTO;
import com.codecool.tarokkgame.service.PlayerService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Objects;

@Controller
public class MessageController {
    private final SimpMessagingTemplate messagingTemplate;
    private final PlayerService playerService;

    public MessageController(SimpMessagingTemplate messagingTemplate, PlayerService playerService) {

        this.messagingTemplate = messagingTemplate;
        this.playerService = playerService;
    }

    @MessageMapping("/game.join")
    public void joinGame(@Payload JoinRequestDTO message, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        String playerName = principal.getName();
        if (playerName.equals(message.username())) {
            JoinMessageDTO joinMessage = playerService.joinGame(playerName);
            if (joinMessage == null) {
                joinMessage = new JoinMessageDTO();
                joinMessage.setInformation("Something went wrong. You cannot join the game.");
                joinMessage.setType("error");
                messagingTemplate.convertAndSendToUser(playerName, "/queue/private", joinMessage);
            } else {
                Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("gameId", joinMessage.getGameId());
                headerAccessor.getSessionAttributes().put("player", playerName);
                joinMessage.setType("game.joined");
                messagingTemplate.convertAndSendToUser(playerName, "/queue/private", joinMessage);
                messagingTemplate.convertAndSend("/topic/game." + joinMessage.getGameId(), joinMessage);
            }
        }
        else {
            throw new NotAllowedOperationException("Invalid username");
        }
    }
}
