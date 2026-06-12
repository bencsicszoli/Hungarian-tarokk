package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.BidLevel;
import com.codecool.tarokkgame.constants.GameState;
import com.codecool.tarokkgame.constants.RoleInGame;
import com.codecool.tarokkgame.model.entity.Game;
import com.codecool.tarokkgame.model.entity.Player;
import com.codecool.tarokkgame.model.entity.RoundResult;
import com.codecool.tarokkgame.repository.GameRepository;
import com.codecool.tarokkgame.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class ResetService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;

    public ResetService(GameRepository gameRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }

    public void resetGame(Game game) {
        game.setXIXInvit(false);
        game.setXVIIIInvit(false);
        game.setYielded(false);
        game.setTarokkInDeclarerSkart(false);
        game.setTarokkInOpponentSkart(false);
        game.setDeclarerAlone(false);
        game.setInvitAcceptor(null);
        game.setBiddingPasses(0);
        game.setInvitedTarokk(0);
        game.setBonusPasses(0);
        game.setTrickRound(1);
        game.setPlayer1TrickCards(0);
        game.setPlayer2TrickCards(0);
        game.setPlayer3TrickCards(0);
        game.setPlayer4TrickCards(0);
        game.setXXICatcher(null);
        game.setTrullAnnouncer(null);
        game.setFourKingsAnnouncer(null);
        game.setDoubleGameAnnouncer(null);
        game.setSuccessfulUltimo(null);
        game.setDeclarer(null);
        game.setLastBonusAnnouncer("declarer");
        game.setTricks(new ArrayList<>(4));
        game.setBidLevel(BidLevel.NONE);
        game.setOptionalBonuses(new HashSet<>());
        game.setDeclarerBonuses(new HashSet<>());
        game.setOpponentBonuses(new HashSet<>());
        gameRepository.save(game); // ?
    }

    public void resetPlayers(Game game) {
        List<Player> players = game.getPlayers();
        for (Player player : players) {
            player.setTarokksInSkart(0);
            player.setAnnouncedXVIII_Invit(false);
            player.setAnnouncedXIX_Invit(false);
            player.setAcceptedXVIII_Invit(false);
            player.setAcceptedXIX_Invit(false);
            player.setYieldedGame(false);
            player.setHasEightTarokks(false);
            player.setHasNineTarokks(false);
            player.setHasPagat(false);
            player.setAnnouncedUltimo(false);
            player.setEightTarokksInAdvance(false);
            player.setNineTarokksInAdvance(false);
            player.setEightTarokksAfterwards(false);
            player.setNineTarokksAfterwards(false);
            player.setBidLevel(BidLevel.NONE);
            player.setRoleInGame(RoleInGame.NOT_CLEAR_YET);
            player.setBidLevels(new HashSet<>());
            player.setResult(null);
            playerRepository.save(player); // ?
        }
    }
}
