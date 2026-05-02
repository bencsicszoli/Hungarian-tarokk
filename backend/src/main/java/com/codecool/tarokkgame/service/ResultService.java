package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.Bonus;
import com.codecool.tarokkgame.constants.RoleInGame;
import com.codecool.tarokkgame.model.entity.*;
import com.codecool.tarokkgame.repository.DeclarerSkartRepository;
import com.codecool.tarokkgame.repository.GameRepository;
import com.codecool.tarokkgame.repository.OwnTrickRepository;
import com.codecool.tarokkgame.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class ResultService {
    private final OwnTrickRepository ownTrickRepository;
    private final GameRepository gameRepository;
    private final DeclarerSkartRepository declarerSkartRepository;
    private final PlayerRepository playerRepository;

    public ResultService(OwnTrickRepository ownTrickRepository, GameRepository gameRepository, DeclarerSkartRepository declarerSkartRepository, PlayerRepository playerRepository) {
        this.ownTrickRepository = ownTrickRepository;
        this.gameRepository = gameRepository;
        this.declarerSkartRepository = declarerSkartRepository;
        this.playerRepository = playerRepository;
    }

    public void setResult(long gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new NoSuchElementException("Game not found"));
        List<OwnTrick> declarerTricks = ownTrickRepository.findAllByPlayerGameAndPlayerRoleInGame(game, RoleInGame.DECLARER);
        List<OwnTrick> partnerTricks = ownTrickRepository.findAllByPlayerGameAndPlayerRoleInGame(game, RoleInGame.DECLARER_PARTNER);
        List<DeclarerSkart> declarerSkartCards = declarerSkartRepository.findAllByGameId(gameId);
        int bidMultiplier = game.getBidLevel().getBidValue();
        System.out.println("bidMultiplier: " + bidMultiplier);
        int cardCount = declarerTricks.size() + partnerTricks.size();
        int cardValues = 0;
        int honours = 0;
        int kings = 0;
        String ultimoWinner = game.getSuccessfulUltimo();
        String XXICatcher = game.getXXICatcher();
        String volatAnnouncer = game.getVolatAnnouncer();
        for (OwnTrick trick : declarerTricks) {
            cardValues += trick.getCardValue();
            if (trick.getCardValue() == 5) {
                if (trick.getCard().getSuit().equals("tarokk")) {
                    honours++;
                } else {
                    kings++;
                }
            }
        }
        for (OwnTrick trick : partnerTricks) {
            cardValues += trick.getCardValue();
            if (trick.getCardValue() == 5) {
                if (trick.getCard().getSuit().equals("tarokk")) {
                    honours++;
                } else {
                    kings++;
                }
            }
        }
        for (DeclarerSkart skart : declarerSkartCards) {
            cardValues += skart.getCardValue();
        }
        System.out.println("Card values: " + cardValues);
        System.out.println("Honours: " + honours);
        System.out.println("Kings: " + kings);
        System.out.println("Card count: " + cardCount);
        Set<Bonus> declarerBonuses = game.getDeclarerBonuses();
        Set<Bonus> opponentBonuses = game.getOpponentBonuses();
        List<Player> players = game.getPlayers();

        Bonus declarerTrull = game.findBonusByIndex(Bonus.TRULL, "declarer");
        Bonus opponentTrull = game.findBonusByIndex(Bonus.TRULL, "opponent");
        Bonus declarerFourKings = game.findBonusByIndex(Bonus.FOUR_KINGS, "declarer");
        Bonus opponentFourKings = game.findBonusByIndex(Bonus.FOUR_KINGS, "opponent");
        for (Player player : players) {
            RoundResult result = new RoundResult();
            //RoundResult result = handleTrull(game, honours, player, declarerTrull, opponentBonus);
            handleTrull(game, honours, player, declarerTrull, opponentTrull, result);
            handleFourKings(game, kings, player, declarerFourKings, opponentFourKings, result);


            result.setPlayer(player); // ?
            player.setResult(result);
        }
        playerRepository.saveAll(players);
        gameRepository.save(game); // ?
    }

    private void handleTrull(Game game, int honours, Player player, Bonus declarerBonus, Bonus opponentBonus, RoundResult roundResult) {
        if (game.getTrullAnnouncer() == null) {
            handleTrullWithoutAnnouncement(honours, player, roundResult);
        } else if (game.getTrullAnnouncer().equals("declarer")) {
            handleTrullWithDeclarerAnnouncer(honours, player, roundResult, declarerBonus);
        } else {
            handleTrullWithOpponentAnnouncer(honours, player, roundResult, opponentBonus);
        }
    }

    private void handleFourKings(Game game, int kings, Player player, Bonus declarerBonus, Bonus opponentBonus, RoundResult result) {
        if (game.getFourKingsAnnouncer() == null) {
            handleFourKingsWithoutAnnouncer(kings, player, result);
        } else if (game.getFourKingsAnnouncer().equals("declarer")) {
            handleFourKingfWithDeclarerAnnouncer(kings, player, result, declarerBonus);
        } else {
            handleFourKingsWithOpponentAnnouncer(kings, player, result, opponentBonus);
        }
    }

    private void handleTrullWithoutAnnouncement(int honours, Player player, RoundResult roundResult) {
        if (honours == 3) { // Declarer has trull, but they did not announce it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                roundResult.setSilentTrull(1);
            } else {
                roundResult.setSilentTrull(-1);
            }
        } else if (honours == 0) { // Opponent has trull, but they did not announce it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                roundResult.setSilentTrull(-1);
            } else {
                roundResult.setSilentTrull(1);
            }
        }
    }

    private void handleFourKingsWithoutAnnouncer(int kings, Player player, RoundResult roundResult) {
        if (kings == 4) {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                roundResult.setSilentFourKings(1);
            } else {
                roundResult.setSilentFourKings(-1);
            }
        } else if (kings == 0) {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                roundResult.setSilentFourKings(-1);
            } else {
                roundResult.setSilentFourKings(1);
            }
        }
    }

    private void handleTrullWithDeclarerAnnouncer(int honours, Player player, RoundResult roundResult, Bonus declarerBonus) {
        if (honours == 3) { // Declarer has trull and they announced it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                setTrullPayment(declarerBonus, roundResult, 1);
            } else {
                setTrullPayment(declarerBonus, roundResult, -1);
            }
        } else { // Declarer does not have trull, but they announced it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                setTrullPayment(declarerBonus, roundResult, -1);
            } else {
                setTrullPayment(declarerBonus, roundResult, 1);
            }
        }
    }

    private void handleFourKingfWithDeclarerAnnouncer(int kings, Player player, RoundResult roundResult, Bonus declarerBonus) {
        if (kings == 4) {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                setFourKingsPayment(declarerBonus, roundResult, 1);
            } else {
                setFourKingsPayment(declarerBonus, roundResult, -1);
            }
        } else {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                setFourKingsPayment(declarerBonus, roundResult, -1);
            } else {
                setFourKingsPayment(declarerBonus, roundResult, 1);
            }
        }
    }

    private void handleTrullWithOpponentAnnouncer(int honours, Player player, RoundResult roundResult, Bonus opponentBonus) {
        if (honours == 0) { // Opponent has trull, and they announce it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                setTrullPayment(opponentBonus, roundResult, -1);
            } else {
                setTrullPayment(opponentBonus, roundResult, 1);
            }
        } else { // Opponent does not have trull, but they announced it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                setTrullPayment(opponentBonus, roundResult, 1);
            } else {
                setTrullPayment(opponentBonus, roundResult, -1);
            }
        }
    }

    private void handleFourKingsWithOpponentAnnouncer(int kings, Player player, RoundResult roundResult, Bonus opponentBonus) {
        if (kings == 0) {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                setFourKingsPayment(opponentBonus, roundResult, -1);
            } else {
                setFourKingsPayment(opponentBonus, roundResult, 1);
            }
        } else {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                setFourKingsPayment(opponentBonus, roundResult, 1);
            } else {
                setFourKingsPayment(opponentBonus, roundResult, -1);
            }
        }
    }

    private void setTrullPayment(Bonus bonus, RoundResult roundResult, int multiplier) {
        if (bonus.getLevel() == 0) {
            roundResult.setTrull(multiplier * bonus.getPointValue());
        } else if (bonus.getLevel() == 1) {
            roundResult.setTrullDoubled(multiplier * bonus.getPointValue());
        } else if (bonus.getLevel() == 2) {
            roundResult.setTrullRedoubled(multiplier * bonus.getPointValue());
        }
    }

    private void setFourKingsPayment(Bonus bonus, RoundResult roundResult, int multiplier) {
        if (bonus.getLevel() == 0) {
            roundResult.setFourKings(multiplier * bonus.getPointValue());
        } else if (bonus.getLevel() == 1) {
            roundResult.setFourKingsDoubled(multiplier * bonus.getPointValue());
        } else if (bonus.getLevel() == 2) {
            roundResult.setFourKingsRedoubled(multiplier * bonus.getPointValue());
        }
    }
}
