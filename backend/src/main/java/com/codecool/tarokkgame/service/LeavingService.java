package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.Bonus;
import com.codecool.tarokkgame.model.PlayerData;
import com.codecool.tarokkgame.model.dto.messagedto.response.PlayerLeaveDTO;
import com.codecool.tarokkgame.model.entity.DeclarerSkart;
import com.codecool.tarokkgame.model.entity.Game;
import com.codecool.tarokkgame.model.entity.Player;
import com.codecool.tarokkgame.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LeavingService {
    private final PlayerRepository playerRepository;
    private final DeckCardRepository deckCardRepository;
    private final DeclarerSkartRepository declarerSkartRepository;
    private final OpponentSkartRepository opponentSkartRepository;
    private final GameRepository gameRepository;
    private final TalonCardRepository talonCardRepository;
    private final MapperService mapperService;
    private final OwnTrickRepository ownTrickRepository;
    private final TrickRepository trickRepository;

    public LeavingService(PlayerRepository playerRepository, DeckCardRepository deckCardRepository, DeclarerSkartRepository declarerSkartRepository, OpponentSkartRepository opponentSkartRepository, PlayerCardRepository playerCardRepository, GameRepository gameRepository, TalonCardRepository talonCardRepository, MapperService mapperService, OwnTrickRepository ownTrickRepository, TrickRepository trickRepository) {
        this.playerRepository = playerRepository;
        this.deckCardRepository = deckCardRepository;
        this.declarerSkartRepository = declarerSkartRepository;
        this.opponentSkartRepository = opponentSkartRepository;
        this.gameRepository = gameRepository;
        this.talonCardRepository = talonCardRepository;
        this.mapperService = mapperService;
        this.ownTrickRepository = ownTrickRepository;
        this.trickRepository = trickRepository;
    }

    public void calculatePenalty(Player player, Game game) {
        StringBuilder penaltyMessage = new StringBuilder();
        penaltyMessage.append("Do you really want to quit this round?@You must pay all bonuses anybody have announced!@");
        int penalty = 0;
        if (!game.getDeclarerBonuses().isEmpty()) {
            for (Bonus bonus : game.getDeclarerBonuses()) {
                penalty += bonus.getPointValue() * 3;
                String bonusName;
                if (bonus.getBonusName().equals("Pass")) {
                    bonusName = "Party";
                } else {
                    bonusName = bonus.getBonusName();
                }
                penaltyMessage.append(bonusName).append(": ").append(bonus.getPointValue() * 3).append(".");
            }
        }
        if (!game.getOpponentBonuses().isEmpty()) {
            for (Bonus bonus : game.getOpponentBonuses()) {
                penalty += bonus.getPointValue() * 3;
                penaltyMessage.append(bonus.getBonusName()).append(": ").append(bonus.getPointValue() * 3).append(".");
            }
        }
        penaltyMessage.append("TOTAL PENALTY: ").append(penalty);
        game.setInformation(penaltyMessage.toString());
        player.setPenalty(penalty);
    }

    public void calculatePenaltyToEventListener(Player player, Game game) {
        int penalty = 0;
        if (!game.getDeclarerBonuses().isEmpty()) {
            for (Bonus bonus : game.getDeclarerBonuses()) {
                penalty += bonus.getPointValue() * 3;
            }
        }
        if (!game.getOpponentBonuses().isEmpty()) {
            for (Bonus bonus : game.getOpponentBonuses()) {
                penalty += bonus.getPointValue() * 3;
            }
        }
        player.setPenalty(penalty);
    }

    public PlayerLeaveDTO leave(Player player, Game game) {
        if (player.getName().equals(game.getPlayer1())) {
            game.setPlayer1(null);
        } else if (player.getName().equals(game.getPlayer2())) {
            game.setPlayer2(null);
        } else if (player.getName().equals(game.getPlayer3())) {
            game.setPlayer3(null);
        } else {
            game.setPlayer4(null);
        }
        if (player.getPenalty() == 0) {
            for (Player tarokkPlayer : game.getPlayers()) {
                tarokkPlayer.getPlayerCards().clear();
                tarokkPlayer.getBidLevels().clear();
                tarokkPlayer.setResult(null); // It is not a must
            }

        } else { // player.getPenalty > 0
            int penalty = player.getPenalty();
            for (Player tarokkPlayer : game.getPlayers()) {
                if (tarokkPlayer.getName().equals(player.getName())) {
                    tarokkPlayer.setBalance(tarokkPlayer.getBalance() - penalty);
                } else {
                    tarokkPlayer.setBalance(tarokkPlayer.getBalance() + penalty/3);
                }
                tarokkPlayer.getPlayerCards().clear();
            }
        }
        deckCardRepository.deleteAllByGameId(game.getId());
        declarerSkartRepository.deleteAllByGameId(game.getId());
        opponentSkartRepository.deleteAllByGameId(game.getId());
        talonCardRepository.deleteAllByGameId(game.getId());
        ownTrickRepository.deleteAllByPlayerGameId(game.getId());
        trickRepository.deleteAllByGameId(game.getId());

        if (game.getPlayers().size() > 1) {
            game.getPlayers().remove(player);
            playerRepository.delete(player);
            game.resetGame();

            Map<String, PlayerData> playersData = mapperService.mapPlayersDataToHashMap(game);
            return new PlayerLeaveDTO(player.getName(), String.format("%s quit the game", player.getName().toUpperCase()), playersData, "game.confirmLeaving");
        } else {  // Only one player remained
            game.getPlayers().remove(player);
            playerRepository.delete(player);
            if (!game.isPrivateGame()) {
                gameRepository.delete(game);
            } else {
                game.resetGame();
                game.setPrivateGame(true);
            }
            return null;
        }
    }
}
