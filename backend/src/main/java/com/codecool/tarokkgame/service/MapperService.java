package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.model.PlayerData;
import com.codecool.tarokkgame.model.dto.messagedto.response.*;
import com.codecool.tarokkgame.model.entity.*;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public PrivateInfoListDTO mapToPrivateResult(Player player) {
        RoundResult result = player.getResult();
        return new PrivateInfoListDTO(result.getInfo(), "game.privateResult");
    }

    public Map<String, PlayerData> mapPlayersDataToHashMap(Game game) {
        Map<String, PlayerData> playersData = new HashMap<>();
        for (Player player : game.getPlayers()) {
            if (player.getName().equals(game.getPlayer1())) {
                PlayerData playerData = new PlayerData(player.getPlayerCards().size(), game.getPlayer1TrickCards(), player.getBalance());
                playersData.put(player.getName(), playerData);
            } else if (player.getName().equals(game.getPlayer2())) {
                PlayerData playerData = new PlayerData(player.getPlayerCards().size(), game.getPlayer2TrickCards(), player.getBalance());
                playersData.put(player.getName(), playerData);
            } else if (player.getName().equals(game.getPlayer3())) {
                PlayerData playerData = new PlayerData(player.getPlayerCards().size(), game.getPlayer3TrickCards(), player.getBalance());
                playersData.put(player.getName(), playerData);
            } else {
                PlayerData playerData = new PlayerData(player.getPlayerCards().size(), game.getPlayer4TrickCards(), player.getBalance());
                playersData.put(player.getName(), playerData);
            }
        }
        return playersData;
    }
}
