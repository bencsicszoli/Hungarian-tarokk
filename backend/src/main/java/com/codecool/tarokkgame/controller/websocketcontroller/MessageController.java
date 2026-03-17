package com.codecool.tarokkgame.controller.websocketcontroller;

import com.codecool.tarokkgame.constants.GameState;
import com.codecool.tarokkgame.exceptionhandling.customexception.NotAllowedOperationException;
import com.codecool.tarokkgame.model.dto.messagedto.*;
import com.codecool.tarokkgame.model.entity.Game;
import com.codecool.tarokkgame.model.entity.Player;
import com.codecool.tarokkgame.repository.GameRepository;
import com.codecool.tarokkgame.repository.PlayerRepository;
import com.codecool.tarokkgame.service.BidService;
import com.codecool.tarokkgame.service.DealService;
import com.codecool.tarokkgame.service.PlayerService;
import com.codecool.tarokkgame.service.ShuffleService;
import jakarta.transaction.Transactional;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Controller
public class MessageController {
    private final SimpMessagingTemplate messagingTemplate;
    private final PlayerService playerService;
    private final GameRepository gameRepository;
    private final ShuffleService shuffleService;
    private final DealService dealService;
    private final PlayerRepository playerRepository;
    private final BidService bidService;

    public MessageController(SimpMessagingTemplate messagingTemplate, PlayerService playerService, GameRepository gameRepository, ShuffleService shuffleService, DealService dealService, PlayerRepository playerRepository, BidService bidService) {

        this.messagingTemplate = messagingTemplate;
        this.playerService = playerService;
        this.gameRepository = gameRepository;
        this.shuffleService = shuffleService;
        this.dealService = dealService;
        this.playerRepository = playerRepository;
        this.bidService = bidService;
    }

    @MessageMapping("/game.join")
    public void joinGame(@Payload JoinRequestDTO message, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        String playerName = principal.getName();
        if (playerName.equals(message.username())) {
            JoinMessageDTO joinMessage = playerService.joinGame(playerName);
            if (joinMessage == null) {
                joinMessage = new JoinMessageDTO();
                joinMessage.setInformation("Something went wrong. You cannot join the game.");
                joinMessage.setType("error"); // Handle the error in the frontend
                messagingTemplate.convertAndSendToUser(playerName, "/queue/private", joinMessage);
                System.out.println("Errormessage sent");
            } else {
                Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("gameId", joinMessage.getGameId());
                headerAccessor.getSessionAttributes().put("player", playerName);
                joinMessage.setType("game.joined");
                messagingTemplate.convertAndSendToUser(playerName, "/queue/private", joinMessage);
                messagingTemplate.convertAndSend("/topic/game." + joinMessage.getGameId(), joinMessage);
                if (joinMessage.getPlayer4() != null) {
                    System.out.println("Asztalfordító meghívódik");
                    JoinMessageDTO joinMessage2 = new JoinMessageDTO();
                    joinMessage2.setInformation("Asztalfordító üzenet");
                    joinMessage2.setType("game.turnTable");
                    messagingTemplate.convertAndSend("/topic/game." + joinMessage.getGameId(), joinMessage2);
                }
            }
        }
        else {
            throw new NotAllowedOperationException("Invalid username");
        }
    }

    @MessageMapping("/game.deal")
    @Transactional
    public void dealCards(@Payload GeneralRequestDTO request, Principal principal) {
        String playerName = principal.getName();
        if (playerName.equals(request.username())) {
            Game game = gameRepository.findById(request.gameId()).orElseThrow(() -> new NoSuchElementException("Game not found"));
            dealTalonCards(game, request);

            int from = 2;
            int to = 6;
            Player playerToDeal = playerRepository.findByUserUsernameAndGameId(request.username(), request.gameId()).orElseThrow(() -> new NoSuchElementException("Dealer not found"));
            for (int i = 0; i < 4; i++) {
                from += 5;
                to += 5;
                playerToDeal = game.getNextPlayer(playerToDeal);
                List<PlayerCardDTO> playerCardDTOS = dealService.getPlayerCards(playerToDeal, from, to);
                dealPlayerCards(playerCardDTOS, playerToDeal, request);
            }

            from++;
            for (int i = 0; i < 4; i++) {
                from += 4;
                to += 4;
                playerToDeal = game.getNextPlayer(playerToDeal);
                List<PlayerCardDTO> playerCardDTOS = dealService.getAllPlayerCards(playerToDeal, from, to);
                dealPlayerCards(playerCardDTOS, playerToDeal, request, i);
            }
            game.setState(GameState.BIDDING);
            gameRepository.save(game);

        } else {
            throw new NotAllowedOperationException("Invalid username");
        }
    }

    @MessageMapping("/game.firstPotentialBids")
    @Transactional
    public void sendPotentialBidsToStartPlayer(@Payload GeneralRequestDTO request, Principal principal) {
        String playerName = principal.getName();
        if (playerName.equals(request.username())) {
            PotentialBidsDTO potentialBids = bidService.getPotentialBidsToStartPlayer(playerName, request.gameId());
            messagingTemplate.convertAndSendToUser(playerName, "/queue/private", potentialBids);
        } else {
            throw new NotAllowedOperationException("Invalid username");
        }
    }

    @MessageMapping("/game.bid")
    @Transactional
    public void sendPotentialBidsToTurnPlayer(@Payload BidLevelRequestDTO request, Principal principal) {
        String playerName = principal.getName();
        if (playerName.equals(request.username())) {
            PotentialBidsDTO potentialBids = bidService.getPotentialBidsToTurnPlayer(playerName, request.gameId(), request.newLevel());
            if (potentialBids != null) {
                Game game = gameRepository.findById(request.gameId()).orElseThrow(() -> new NoSuchElementException("Game not found"));
                String turnPlayer = game.getTurnPlayer();
                messagingTemplate.convertAndSendToUser(turnPlayer, "/queue/private", potentialBids);
            }

        } else {
            throw new NotAllowedOperationException("Invalid username");
        }
    }

    private void dealTalonCards(Game game, GeneralRequestDTO request) {
        shuffleService.addShuffledDeck(game);
        dealService.setTalonCards(game);
        PublicTalonDTO publicTalonDTO = new PublicTalonDTO(6, "game.talon", "IN_PROGRESS");
        messagingTemplate.convertAndSend("/topic/game." + request.gameId(), publicTalonDTO);
    }

    private void dealPlayerCards(List<PlayerCardDTO> playerCardDTOS, Player playerToDeal, GeneralRequestDTO request) {
        PlayerCardListDTO playerCardListDTO = new PlayerCardListDTO(playerCardDTOS, "game.playerCards");
        messagingTemplate.convertAndSendToUser(playerToDeal.getName(), "/queue/private", playerCardListDTO);
        PublicCardsNumberDTO cardNumberDTO = new PublicCardsNumberDTO(5, "game.cardNumber", playerToDeal.getName());
        messagingTemplate.convertAndSend("/topic/game." + request.gameId(), cardNumberDTO);
    }

    private void dealPlayerCards(List<PlayerCardDTO> playerCardDTOS, Player playerToDeal, GeneralRequestDTO request, int i) {
        PlayerCardListDTO playerCardListDTO = new PlayerCardListDTO(playerCardDTOS, "game.playerCards");
        messagingTemplate.convertAndSendToUser(playerToDeal.getName(), "/queue/private", playerCardListDTO);
        PublicCardsNumberDTO cardNumberDTO;
        if (i == 3) {
            cardNumberDTO = new PublicCardsNumberDTO(9, "game.lastDeal", playerToDeal.getName());
        } else {
            cardNumberDTO = new PublicCardsNumberDTO(9, "game.cardNumber", playerToDeal.getName());
        }
        messagingTemplate.convertAndSend("/topic/game." + request.gameId(), cardNumberDTO);
    }

}
