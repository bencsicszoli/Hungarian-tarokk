package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.Bonus;
import com.codecool.tarokkgame.constants.MessageKey;
import com.codecool.tarokkgame.model.PlayerData;
import com.codecool.tarokkgame.model.dto.LocalizedMessage;
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
        List<LocalizedMessage> penaltyMessage = new ArrayList<>();
        penaltyMessage.add(new LocalizedMessage(MessageKey.LEAVE_CONFIRM_QUIT));
        penaltyMessage.add(new LocalizedMessage(MessageKey.LEAVE_MUST_PAY_BONUSES));
        int penalty = 0;
        if (!game.getDeclarerBonuses().isEmpty()) {
            methodIfDeclarerBonusesNotEmpty(game, penalty, penaltyMessage);
        }
        if (!game.getOpponentBonuses().isEmpty()) {
            methodIfOpponentBonusesNotEmpty(game, penalty, penaltyMessage);
        }
        penaltyMessage.add(new LocalizedMessage(MessageKey.LEAVE_TOTAL_PENALTY, Map.of("penalty", penalty)));
        game.setInformation(penaltyMessage);
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
        setPlayerToNull(player, game);
        if (player.getPenalty() == 0) {
            methodWithoutPenalty(game);
        } else { // player.getPenalty > 0
            methodWithPenalty(player, game);
        }
        resetRepositories(game);

        if (game.getPlayers().size() > 1) {
            return methodAtSeveralPlayerRemained(player, game);

        } else {  // Only one player remained
            methodWithOnePlayerRemained(player, game);
            return null;
        }
    }

    private void methodIfDeclarerBonusesNotEmpty(Game game, int penalty, List<LocalizedMessage> penaltyMessage) {
        for (Bonus bonus : game.getDeclarerBonuses()) {
            penalty += bonus.getPointValue() * 3;
            penaltyMessage.add(new LocalizedMessage(MessageKey.LEAVE_BONUS_PENALTY_LINE, Map.of("bonusName", bonus.getLocalizedSummaryName(), "points", bonus.getPointValue() * 3)));
        }
    }

    private void methodIfOpponentBonusesNotEmpty(Game game, int penalty, List<LocalizedMessage> penaltyMessage) {
        for (Bonus bonus : game.getOpponentBonuses()) {
            penalty += bonus.getPointValue() * 3;
            penaltyMessage.add(new LocalizedMessage(MessageKey.LEAVE_BONUS_PENALTY_LINE, Map.of("bonusName", bonus.getLocalizedSummaryName(), "points", bonus.getPointValue() * 3)));
        }
    }

    private void setPlayerToNull(Player player, Game game) {
        if (player.getName().equals(game.getPlayer1())) {
            game.setPlayer1(null);
        } else if (player.getName().equals(game.getPlayer2())) {
            game.setPlayer2(null);
        } else if (player.getName().equals(game.getPlayer3())) {
            game.setPlayer3(null);
        } else {
            game.setPlayer4(null);
        }
    }

    private void methodWithoutPenalty(Game game) {
        for (Player tarokkPlayer : game.getPlayers()) {
            tarokkPlayer.getPlayerCards().clear();
            tarokkPlayer.getBidLevels().clear();
            tarokkPlayer.setResult(null); // It is not a must
        }
    }

    private void methodWithPenalty(Player player, Game game) {
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

    private void resetRepositories(Game game) {
        deckCardRepository.deleteAllByGameId(game.getId());
        declarerSkartRepository.deleteAllByGameId(game.getId());
        opponentSkartRepository.deleteAllByGameId(game.getId());
        talonCardRepository.deleteAllByGameId(game.getId());
        ownTrickRepository.deleteAllByPlayerGameId(game.getId());
        trickRepository.deleteAllByGameId(game.getId());
    }

    private PlayerLeaveDTO methodAtSeveralPlayerRemained(Player player, Game game) {
        game.getPlayers().remove(player);
        playerRepository.delete(player);
        game.resetGame();

        Map<String, PlayerData> playersData = mapperService.mapPlayersDataToHashMap(game);
        LocalizedMessage quitMessage = new LocalizedMessage(MessageKey.LEAVE_PLAYER_QUIT, Map.of("username", player.getName().toUpperCase()));
        return new PlayerLeaveDTO(player.getName(), quitMessage, playersData, "game.confirmLeaving");
    }

    private void methodWithOnePlayerRemained(Player player, Game game) {
        game.getPlayers().remove(player);
        playerRepository.delete(player);
        if (!game.isPrivateGame()) {
            gameRepository.delete(game);
        } else {
            game.resetGame();
            game.setPrivateGame(true);
        }
    }
}
