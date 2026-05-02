package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.model.dto.messagedto.response.JoinMessageDTO;
import com.codecool.tarokkgame.model.dto.messagedto.response.PlayerCardDTO;
import com.codecool.tarokkgame.model.dto.messagedto.response.PrivateResultDTO;
import com.codecool.tarokkgame.model.dto.messagedto.response.TrickCardDTO;
import com.codecool.tarokkgame.model.entity.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MapperService {

    public JoinMessageDTO mapToJoinMessageDTO(Game game, List<Player> players) {
        JoinMessageDTO message = new JoinMessageDTO();
        message.setPlayer1(game.getPlayer1());
        message.setPlayer2(game.getPlayer2());
        message.setPlayer3(game.getPlayer3());
        message.setPlayer4(game.getPlayer4());
        message.setGameId(game.getId());
        if (players.size() == 4) {
            message.setDealer(game.getDealer());
            message.setTurnPlayer(game.getTurnPlayer());
            message.setStartPlayer(game.getStartPlayer());
        }

        message.setInformation(game.getInformation());
        message.setPlayer1Balance(players.getFirst().getBalance());
        for (Player player : players) {
            if (player.getPlace() == 1) {
                message.setPlayer1Balance(player.getBalance());
            } else if (player.getPlace() == 2) {
                message.setPlayer2Balance(player.getBalance());
            } else if (player.getPlace() == 3) {
                message.setPlayer3Balance(player.getBalance());
            } else if (player.getPlace() == 4) {
                message.setPlayer4Balance(player.getBalance());
            }
        }
        return message;
    }

    public List<PlayerCardDTO> mapToPlayerCardListDTO(List<PlayerCard> playerCards) {
        List<PlayerCardDTO> playerCardDTOs = new ArrayList<>();
        for (PlayerCard playerCard : playerCards) {
            PlayerCardDTO dto = new PlayerCardDTO(
                    playerCard.getCard().getId(),
                    playerCard.getPlayer().getId(),
                    playerCard.getCard().getFrontImagePath(),
                    playerCard.isClickable());
            playerCardDTOs.add(dto);
        }
        return playerCardDTOs;
    }

    public List<TrickCardDTO> mapToTrickCardListDTO(List<Trick> tricks) {
        List<TrickCardDTO> trickCardDTOList = new ArrayList<>();
        for (Trick trickCard : tricks) {
            TrickCardDTO trickCardDTO = new TrickCardDTO(
                    trickCard.getCard().getId(),
                    trickCard.getPlayer().getId(),
                    trickCard.getCard().getFrontImagePath(),
                    trickCard.getX(),
                    trickCard.getY(),
                    trickCard.getRotation());
            trickCardDTOList.add(trickCardDTO);
        }
        return trickCardDTOList;
    }

    public List<OwnTrick> mapToOwnTrickList(List<Trick> tricks, Player trickWinner) {
        List<OwnTrick> wonCards = new ArrayList<>();
        for (Trick trick : tricks) {
            OwnTrick ownTrick = new OwnTrick();
            ownTrick.setCard(trick.getCard());
            ownTrick.setPlayer(trickWinner);
            wonCards.add(ownTrick);
        }
        return wonCards;
    }

    public PrivateResultDTO mapToPrivateResult(Player player) {
        RoundResult result = player.getResult();
        PrivateResultDTO dto = new PrivateResultDTO();
        dto.setParty(result.getParty());
        dto.setPartyDoubled(result.getPartyDoubled());
        dto.setPartyRedoubled(result.getPartyRedoubled());
        dto.setSilentTrull(result.getSilentTrull());
        dto.setTrull(result.getTrull());
        dto.setTrullDoubled(result.getTrullDoubled());
        dto.setTrullRedoubled(result.getTrullRedoubled());
        dto.setSilentFourKings(result.getSilentFourKings());
        dto.setFourKings(result.getFourKings());
        dto.setFourKingsDoubled(result.getFourKingsDoubled());
        dto.setFourKingsRedoubled(result.getFourKingsRedoubled());
        dto.setSilentDoubleGame(result.getSilentDoubleGame());
        dto.setDoubleGame(result.getDoubleGame());
        dto.setDoubleGameDoubled(result.getDoubleGameDoubled());
        dto.setDoubleGameRedoubled(result.getDoubleGameRedoubled());
        dto.setSilentUltimo(result.getSilentUltimo());
        dto.setUltimo(result.getUltimo());
        dto.setUltimoDoubled(result.getUltimoDoubled());
        dto.setUltimoRedoubled(result.getUltimoRedoubled());
        dto.setSilentXXICatch(result.getSilentXXICatch());
        dto.setXXICatch(result.getXXICatch());
        dto.setXXICatchDoubled(result.getXXICatchDoubled());
        dto.setXXICatchRedoubled(result.getXXICatchRedoubled());
        dto.setSilentVolat(result.getSilentVolat());
        dto.setVolat(result.getVolat());
        dto.setVolatDoubled(result.getVolatDoubled());
        dto.setVolatRedoubled(result.getVolatRedoubled());
        dto.setEightTarokksInAdvance(result.getEightTarokksInAdvance());
        dto.setNineTarokksInAdvance(result.getNineTarokksInAdvance());
        dto.setEightTarokksAfterwards(result.getEightTarokksAfterwards());
        dto.setNineTarokksAfterwards(result.getNineTarokksAfterwards());
        dto.setType("game.privateResult");
        return dto;
    }
}
