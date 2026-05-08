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
        int cardCount = declarerTricks.size() + partnerTricks.size();
        int cardValues = 0;
        int honours = 0;
        int kings = 0;
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
        List<Player> players = game.getPlayers();

        Bonus party = game.findBonusByIndex(Bonus.PASS, "declarer");

        for (Player player : players) {
            RoundResult result = new RoundResult();
            //RoundResult result = handleTrull(game, honours, player, declarerTrull, opponentBonus);
            handleTrull(game, honours, player, result);
            handleFourKings(game, kings, player, result);
            handlePagatUltimo(game, player, result);
            handleXXICatch(game, player, result);
            handleVolat(player, game, result, party, cardCount, cardValues, bidMultiplier);
            handleDoubleGame(player, result, party, cardCount, cardValues, bidMultiplier, game);
            result.setSum();
            result.setPlayer(player); // ?
            player.setResult(result);
        }
        playerRepository.saveAll(players);
        gameRepository.save(game); // ?
    }

    private void handleTrull(Game game, int honours, Player player, RoundResult roundResult) {
        Bonus declarerTrull = game.findBonusByIndex(Bonus.TRULL, "declarer");
        Bonus opponentTrull = game.findBonusByIndex(Bonus.TRULL, "opponent");
        if (game.getTrullAnnouncer() == null) {
            handleTrullWithoutAnnouncement(honours, player, roundResult, game);
        } else if (game.getTrullAnnouncer().equals("declarer")) {
            handleTrullWithDeclarerAnnouncer(honours, player, roundResult, declarerTrull, game);
        } else {
            handleTrullWithOpponentAnnouncer(honours, player, roundResult, opponentTrull, game);
        }
    }

    private void handleFourKings(Game game, int kings, Player player, RoundResult result) {
        Bonus declarerFourKings = game.findBonusByIndex(Bonus.FOUR_KINGS, "declarer");
        Bonus opponentFourKings = game.findBonusByIndex(Bonus.FOUR_KINGS, "opponent");
        if (game.getFourKingsAnnouncer() == null) {
            handleFourKingsWithoutAnnouncer(kings, player, result, game);
        } else if (game.getFourKingsAnnouncer().equals("declarer")) {
            handleFourKingfWithDeclarerAnnouncer(kings, player, result, declarerFourKings, game);
        } else {
            handleFourKingsWithOpponentAnnouncer(kings, player, result, opponentFourKings, game);
        }
    }

    private void handlePagatUltimo(Game game, Player player, RoundResult result) {
        Bonus declarerUltimo = game.findBonusByIndex(Bonus.PAGAT_ULTIMO, "declarer");
        Bonus opponentUltimo = game.findBonusByIndex(Bonus.PAGAT_ULTIMO, "opponent");
        if (game.getSuccessfulUltimo() == null) { // No one has ultimo
            handleUltimoNoOneAchieved(player, result, declarerUltimo, opponentUltimo, game);
        } else if (game.getSuccessfulUltimo().equals("declarer")) { // Declarer has ultimo
            handleUltimoDeclarerAchieved(player, result, declarerUltimo, game);
        } else { // Opponent has ultimo
            handleUltimoOpponentAchieved(player, result, opponentUltimo, game);
        }
    }

    private void handleXXICatch(Game game, Player player, RoundResult result) {
        Bonus declarerXXICatch = game.findBonusByIndex(Bonus.XXI_CATCH, "declarer");
        Bonus opponentXXICatch = game.findBonusByIndex(Bonus.XXI_CATCH, "opponent");
        if (game.getXXICatcher() == null) { // No one has ultimo
            handleXXICatchNoOneAchieved(player, result, declarerXXICatch, opponentXXICatch, game);
        } else if (game.getXXICatcher().equals("declarer")) { // Declarer has ultimo
            handleXXICatchDeclarerAchieved(player, result, declarerXXICatch, game);
        } else { // Opponent has ultimo
            handleXXICatchOpponentAchieved(player, result, opponentXXICatch, game);
        }
    }

    private void handleVolat(Player player, Game game, RoundResult result, Bonus party, int cardCount, int cardValues, int bidMultiplier) {
        Bonus declarerVolat = game.findBonusByIndex(Bonus.VOLAT, "declarer");
        Bonus opponentVolat = game.findBonusByIndex(Bonus.VOLAT, "opponent");
        switch (cardCount) {
            case 36: handleVolatDeclarerAchieved(player, result, declarerVolat, opponentVolat, bidMultiplier, game); break;
            case 0: handleVolatOpponentAchieved(player, result, declarerVolat, opponentVolat, bidMultiplier, game); break;
            default: handleVolatNoOneAchieved(player, result, declarerVolat, opponentVolat, bidMultiplier, game); break;
        }
        if (party.getLevel() > 0) {
            handlePartyDoubledOrRedoubled(player, result, party, cardValues, bidMultiplier, game);
        } else { // Party level = 0
            if (cardCount > 0 && cardCount < 36 && declarerVolat != null && cardValues < 48) {
                if (player.getRoleInGame().getTeam().equals("opponent")) {
                    result.setParty(party.getPointValue() * bidMultiplier);
                } else {
                    if (!game.isDeclarerAlone()) {
                        result.setParty(-party.getPointValue() * bidMultiplier);
                    } else {
                        result.setParty(-3 * party.getPointValue() * bidMultiplier);
                    }

                }
            } else if (cardCount > 0 && cardCount < 36 && opponentVolat != null && cardValues > 47) {
                if (player.getRoleInGame().getTeam().equals("declarer")) {
                    if (!game.isDeclarerAlone()) {
                        result.setParty(party.getPointValue() * bidMultiplier);
                    } else {
                        result.setParty(3 * party.getPointValue() * bidMultiplier);
                    }
                } else {
                    result.setParty(-party.getPointValue() * bidMultiplier);
                }
            }
        }
    }

    private void handleDoubleGame(Player player, RoundResult result, Bonus party, int cardCount, int cardValues, int bidMultiplier, Game game) {
        Bonus declarerDoubleGame = game.findBonusByIndex(Bonus.DOUBLE, "declarer");
        Bonus opponentDoubleGame = game.findBonusByIndex(Bonus.DOUBLE, "opponent");
        if (cardValues > 70) { // Declarer has double game
            handleDoubleGameDeclarerAchieved(player, result, declarerDoubleGame, opponentDoubleGame, cardCount, cardValues, game);
        } else if (cardValues < 24) { // opponent has double game
            handleDoubleGameOpponentAchieved(player, result, declarerDoubleGame, opponentDoubleGame, cardCount, bidMultiplier, game);
        } else { // Neither side has double game
            handleDoubleGameNoOneAchieved(player, result, declarerDoubleGame, opponentDoubleGame, bidMultiplier, game);
        }
        if (party.getLevel() > 0) {
            handlePartyDoubledOrRedoubled(player, result, party, cardValues, bidMultiplier, game);
        } else { // Party was not doubled
            handlePartyNotDoubled(player, result, declarerDoubleGame, opponentDoubleGame, party, cardValues, bidMultiplier, game);
        }
    }

    // Refactor!
    private void handlePartyDoubledOrRedoubled(Player player, RoundResult result, Bonus party, int cardValues, int bidMultiplier, Game game) {
        if (cardValues > 47) {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setHigherLevelPartyPayment(party, result, bidMultiplier, 1);
                } else {
                    setHigherLevelPartyPayment(party, result, bidMultiplier, 3);
                }

            } else { // team = "opponent"
                setHigherLevelPartyPayment(party, result, bidMultiplier, -1);
            }
        } else if (cardValues < 47) {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setHigherLevelPartyPayment(party, result, bidMultiplier, -1);
                } else {
                    setHigherLevelPartyPayment(party, result, bidMultiplier, -3);
                }

            } else { // team = "opponent"
                setHigherLevelPartyPayment(party, result, bidMultiplier, 1);
            }
        } else { // cardValue = 47;
            if (party.getLevel() == 1) {
                if (player.getRoleInGame().getTeam().equals("declarer")) {
                    if (!game.isDeclarerAlone()) {
                        result.setPartyDoubled(party.getPointValue() * bidMultiplier);
                    } else {
                        result.setPartyDoubled(3 * party.getPointValue() * bidMultiplier);
                    }

                } else {
                    result.setPartyRedoubled(-party.getPointValue() * bidMultiplier);
                }
            } else { // level = 2
                if (player.getRoleInGame().getTeam().equals("declarer")) {
                    if (!game.isDeclarerAlone()) {
                        result.setPartyDoubled(-party.getPointValue() * bidMultiplier);
                    } else {
                        result.setPartyDoubled(-3 * party.getPointValue() * bidMultiplier);
                    }

                } else {
                    result.setPartyRedoubled(party.getPointValue() * bidMultiplier);
                }
            }
        }
    }

    private void handlePartyNotDoubled(Player player, RoundResult result, Bonus declarerDoubleGame, Bonus opponentDoubleGame, Bonus party, int cardValues, int bidMultiplier, Game game) {
        if (cardValues < 48 && cardValues > 23 && declarerDoubleGame != null) {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    result.setParty(-party.getPointValue() * bidMultiplier);
                } else {
                    result.setParty(-3 * party.getPointValue() * bidMultiplier);
                }

            } else {
                result.setParty(party.getPointValue() * bidMultiplier);
            }
        } else if (cardValues > 47 && cardValues < 71 && opponentDoubleGame != null) {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    result.setParty(party.getPointValue() * bidMultiplier);
                } else {
                    result.setParty(3 * party.getPointValue() * bidMultiplier);
                }

            } else {
                result.setParty(-party.getPointValue() * bidMultiplier);
            }
        }
    }

    private void handleTrullWithoutAnnouncement(int honours, Player player, RoundResult roundResult, Game game) {
        if (honours == 3) { // Declarer has trull, but they did not announce it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    roundResult.setSilentTrull(1);
                } else {
                    roundResult.setSilentTrull(3);
                }

            } else {
                roundResult.setSilentTrull(-1);
            }
        } else if (honours == 0) { // Opponent has trull, but they did not announce it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    roundResult.setSilentTrull(-1);
                } else {
                    roundResult.setSilentTrull(-3);
                }
            } else {
                roundResult.setSilentTrull(1);
            }
        }
    }

    private void handleFourKingsWithoutAnnouncer(int kings, Player player, RoundResult roundResult, Game game) {
        if (kings == 4) {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    roundResult.setSilentFourKings(1);
                } else {
                    roundResult.setSilentFourKings(3);
                }

            } else {
                roundResult.setSilentFourKings(-1);
            }
        } else if (kings == 0) {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    roundResult.setSilentFourKings(-1);
                } else {
                    roundResult.setSilentFourKings(-3);
                }
            } else {
                roundResult.setSilentFourKings(1);
            }
        }
    }

    private void handleUltimoNoOneAchieved(Player player, RoundResult result, Bonus declarerUltimo, Bonus opponentUltimo, Game game) {
        if (declarerUltimo != null) { // but declarer announced it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setUltimoPayment(declarerUltimo, result, -1);
                } else {
                    setUltimoPayment(opponentUltimo, result, -3);
                }
            } else {
                setUltimoPayment(declarerUltimo, result, 1);
            }
        } else if (opponentUltimo != null) {
            if (player.getRoleInGame().getTeam().equals("declarer")) { // but opponent announced it
                if (!game.isDeclarerAlone()) {
                    setUltimoPayment(opponentUltimo, result, 1);
                } else {
                    setUltimoPayment(opponentUltimo, result, 3);
                }
            } else {
                setUltimoPayment(opponentUltimo, result, -1);
            }
        }
    }

    private void handleXXICatchNoOneAchieved(Player player, RoundResult result, Bonus declarerXXICatch, Bonus opponentXXICatch, Game game) {
        if (declarerXXICatch != null) { // but declarer announced it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setXXICatchPayment(declarerXXICatch, result, -1);
                } else {
                    setXXICatchPayment(declarerXXICatch, result, -3);
                }

            } else {
                setXXICatchPayment(declarerXXICatch, result, 1);
            }
        } else if (opponentXXICatch != null) {
            if (player.getRoleInGame().getTeam().equals("declarer")) { // but opponent announced it
                if (!game.isDeclarerAlone()) {
                    setXXICatchPayment(opponentXXICatch, result, 1);
                } else {
                    setXXICatchPayment(opponentXXICatch, result, 3);
                }
            } else {
                setXXICatchPayment(opponentXXICatch, result, -1);
            }
        }
    }

    private void handleVolatNoOneAchieved(Player player, RoundResult result, Bonus declarerVolat, Bonus opponentVolat, int bidMultiplier, Game game) {
        if (declarerVolat != null) {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setNormalVolatPayment(declarerVolat, result, bidMultiplier, -1);
                } else {
                    setNormalVolatPayment(declarerVolat, result, bidMultiplier, -3);
                }

            } else {
                setNormalVolatPayment(declarerVolat, result, bidMultiplier, 1);
            }
        } else if (opponentVolat != null) {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setNormalVolatPayment(opponentVolat, result, bidMultiplier, 1);
                } else {
                    setNormalVolatPayment(opponentVolat, result, bidMultiplier, 3);
                }

            } else {
                setNormalVolatPayment(opponentVolat, result, bidMultiplier, -1);
            }
        }
    }

    private void handleDoubleGameNoOneAchieved(Player player, RoundResult result, Bonus declarerDoubleGame, Bonus opponentDoubleGame, int bidMultiplier, Game game) {
        if (declarerDoubleGame != null) { // Declarer's double game failed
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setDoubleGamePayment(declarerDoubleGame, result, bidMultiplier, -1);
                } else {
                    setDoubleGamePayment(declarerDoubleGame, result, bidMultiplier, -3);
                }

            } else {
                setDoubleGamePayment(declarerDoubleGame, result, bidMultiplier, 1);
            }
        } else { // Opponent's double game failed
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setDoubleGamePayment(opponentDoubleGame, result, bidMultiplier, 1);
                } else {
                    setDoubleGamePayment(opponentDoubleGame, result, bidMultiplier, 3);
                }
            } else {
                setDoubleGamePayment(opponentDoubleGame, result, bidMultiplier, -1);
            }
        }
    }

    // Refactor?
    private void handleTrullWithDeclarerAnnouncer(int honours, Player player, RoundResult roundResult, Bonus declarerBonus, Game game) {
        if (honours == 3) { // Declarer has trull and they announced it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setTrullPayment(declarerBonus, roundResult, 1);
                } else {
                    setTrullPayment(declarerBonus, roundResult, 3);
                }

            } else {
                setTrullPayment(declarerBonus, roundResult, -1);
            }
        } else if (honours == 1 || honours == 2) { // Declarer does not have trull, but they announced it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setTrullPayment(declarerBonus, roundResult, -1);
                } else {
                    setTrullPayment(declarerBonus, roundResult, -3);
                }
            } else {
                setTrullPayment(declarerBonus, roundResult, 1);
            }
        } else { // Declarer announced trull, but opponent has the three honours
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setTrullPayment(declarerBonus, roundResult, -1);
                    roundResult.setSilentTrull(-1);
                } else {
                    setTrullPayment(declarerBonus, roundResult, -3);
                    roundResult.setSilentTrull(-3);
                }

            } else {
                setTrullPayment(declarerBonus, roundResult, 1);
                roundResult.setSilentTrull(1);
            }
        }
    }

    // Refactor?
    private void handleFourKingfWithDeclarerAnnouncer(int kings, Player player, RoundResult roundResult, Bonus declarerBonus, Game game) {
        if (kings == 4) {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setFourKingsPayment(declarerBonus, roundResult, 1);
                } else {
                    setFourKingsPayment(declarerBonus, roundResult, 3);
                }

            } else {
                setFourKingsPayment(declarerBonus, roundResult, -1);
            }
        } else if (kings > 0 && kings < 4) {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setFourKingsPayment(declarerBonus, roundResult, -1);
                } else {
                    setFourKingsPayment(declarerBonus, roundResult, -3);
                }
            } else {
                setFourKingsPayment(declarerBonus, roundResult, 1);
            }
        } else {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setFourKingsPayment(declarerBonus, roundResult, -1);
                    roundResult.setSilentFourKings(-1);
                } else {
                    setFourKingsPayment(declarerBonus, roundResult, -3);
                    roundResult.setSilentFourKings(-3);
                }

            } else {
                setFourKingsPayment(declarerBonus, roundResult, 1);
                roundResult.setSilentFourKings(1);
            }
        }
    }

    private void handleUltimoDeclarerAchieved(Player player, RoundResult result, Bonus declarerUltimo, Game game) {
        if (declarerUltimo == null) { // but they did not announce it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    result.setSilentUltimo(5);
                } else {
                    result.setSilentUltimo(15);
                }
            } else {
                result.setSilentUltimo(-5);
            }
        } else { // and they announced it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setUltimoPayment(declarerUltimo, result, 1);
                } else {
                    setUltimoPayment(declarerUltimo, result, 3);
                }

            } else {
                setUltimoPayment(declarerUltimo, result, -1);
            }
        }
    }

    private void handleXXICatchDeclarerAchieved(Player player, RoundResult result, Bonus declarerXXICatch, Game game) {
        if (declarerXXICatch == null) { // but they did not announce it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    result.setSilentXXICatch(21);
                } else {
                    result.setSilentXXICatch(63);
                }

            } else {
                result.setSilentXXICatch(-21);
            }
        } else { // and they announced it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setXXICatchPayment(declarerXXICatch, result, 1);
                } else {
                    setXXICatchPayment(declarerXXICatch, result, 3);
                }

            } else {
                setXXICatchPayment(declarerXXICatch, result, -1);
            }
        }
    }

    private void handleVolatDeclarerAchieved(Player player, RoundResult result, Bonus declarerVolat, Bonus opponentVolat, int bidMultiplier, Game game) {
        if (declarerVolat == null) { // Declarer did not announce it
            if (opponentVolat == null) { // Opponent also did not announce it
                if (player.getRoleInGame().getTeam().equals("declarer")) {
                    if (!game.isDeclarerAlone()) {
                        result.setSilentVolat(3 * bidMultiplier);
                    } else {
                        result.setSilentVolat(9 * bidMultiplier);
                    }

                } else {
                    result.setSilentVolat(-3 * bidMultiplier);
                }
            } else { // Declarer has silent volat, but opponent announced it (very edge case)
                if (player.getRoleInGame().getTeam().equals("declarer")) {
                    if (!game.isDeclarerAlone()) {
                        setEdgeCaseVolatPayment(opponentVolat, result, bidMultiplier, 1);
                    } else {
                        setEdgeCaseVolatPayment(opponentVolat, result, bidMultiplier, 3);
                    }

                } else {
                    setEdgeCaseVolatPayment(opponentVolat, result, bidMultiplier, -1);
                }
            }
        } else { // Declarer has volat, and they announced it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setNormalVolatPayment(declarerVolat, result, bidMultiplier, 1);
                } else {
                    setNormalVolatPayment(declarerVolat, result, bidMultiplier, 3);
                }

            } else {
                setNormalVolatPayment(declarerVolat, result, bidMultiplier, -1);
            }
        }
    }

    private void handleDoubleGameDeclarerAchieved(Player player, RoundResult result, Bonus declarerDoubleGame, Bonus opponentDoubleGame, int cardCount, int bidMultiplier, Game game) {
        if (declarerDoubleGame != null) { // Declarer announced double game
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                setDoubleGamePayment(declarerDoubleGame, result, bidMultiplier, 1);
            } else {
                setDoubleGamePayment(declarerDoubleGame, result, bidMultiplier, -1);
            }
        } else { // Declarer did not announce double game
            if (opponentDoubleGame != null) { // Opponent announced double game (very edge case)
                if (player.getRoleInGame().getTeam().equals("declarer")) {
                    if (!game.isDeclarerAlone()) {
                        setDoubleGamePayment(opponentDoubleGame, result, bidMultiplier, 1);
                    } else {
                        setDoubleGamePayment(opponentDoubleGame, result, bidMultiplier, 3);
                    }

                    if (cardCount < 36) {
                        result.setSilentDoubleGame(opponentDoubleGame.getPointValue() * bidMultiplier);
                    }
                } else {
                    setDoubleGamePayment(opponentDoubleGame, result, bidMultiplier, -1);
                    if (cardCount < 36) {
                        result.setSilentDoubleGame(-opponentDoubleGame.getPointValue() * bidMultiplier);
                    }
                }
            } else { // Neither side announced double game
                if (cardCount < 36) {
                    if (player.getRoleInGame().getTeam().equals("declarer")) {
                        if (!game.isDeclarerAlone()) {
                            result.setSilentDoubleGame(4 * bidMultiplier);
                        } else {
                            result.setSilentDoubleGame(12 * bidMultiplier);
                        }

                    } else {
                        result.setSilentDoubleGame(-4 * bidMultiplier);
                    }
                }
            }
        }
    }

    private void handleTrullWithOpponentAnnouncer(int honours, Player player, RoundResult roundResult, Bonus opponentBonus, Game game) {
        if (honours == 0) { // Opponent has trull, and they announce it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setTrullPayment(opponentBonus, roundResult, -1);
                } else {
                    setTrullPayment(opponentBonus, roundResult, -3);
                }
            } else {
                setTrullPayment(opponentBonus, roundResult, 1);
            }
        } else if (honours == 1 || honours == 2) { // Opponent does not have trull, but they announced it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setTrullPayment(opponentBonus, roundResult, 1);
                } else {
                    setTrullPayment(opponentBonus, roundResult, 3);
                }
            } else {
                setTrullPayment(opponentBonus, roundResult, -1);
            }
        } else { // Opponent announced trull, but declarer has the three honours
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setTrullPayment(opponentBonus, roundResult, 1);
                    roundResult.setSilentTrull(1);
                } else {
                    setTrullPayment(opponentBonus, roundResult, 3);
                    roundResult.setSilentTrull(3);
                }
            } else {
                setTrullPayment(opponentBonus, roundResult, -1);
                roundResult.setSilentTrull(-1);
            }
        }
    }

    private void handleFourKingsWithOpponentAnnouncer(int kings, Player player, RoundResult roundResult, Bonus opponentBonus, Game game) {
        if (kings == 0) {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setFourKingsPayment(opponentBonus, roundResult, -1);
                } else {
                    setFourKingsPayment(opponentBonus, roundResult, -3);
                }

            } else {
                setFourKingsPayment(opponentBonus, roundResult, 1);
            }
        } else if (kings > 0 && kings < 4) {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setFourKingsPayment(opponentBonus, roundResult, 1);
                } else {
                    setFourKingsPayment(opponentBonus, roundResult, 3);
                }

            } else {
                setFourKingsPayment(opponentBonus, roundResult, -1);
            }
        } else {
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setFourKingsPayment(opponentBonus, roundResult, 1);
                    roundResult.setSilentFourKings(1);
                } else {
                    setFourKingsPayment(opponentBonus, roundResult, 3);
                    roundResult.setSilentFourKings(3);
                }

            } else {
                setFourKingsPayment(opponentBonus, roundResult, -1);
                roundResult.setSilentFourKings(-1);
            }
        }
    }

    private void handleUltimoOpponentAchieved(Player player, RoundResult result, Bonus opponentUltimo, Game game) {
        if (opponentUltimo == null) { // but they did not announce it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    result.setSilentUltimo(-5);
                } else {
                    result.setSilentUltimo(-15);
                }
            } else {
                result.setSilentUltimo(5);
            }
        } else { // and they announced it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setUltimoPayment(opponentUltimo, result, -1);
                } else {
                    setUltimoPayment(opponentUltimo, result, -3);
                }

            } else {
                setUltimoPayment(opponentUltimo, result, 1);
            }
        }
    }

    private void handleXXICatchOpponentAchieved(Player player, RoundResult result, Bonus opponentXXICatch, Game game) {
        if (opponentXXICatch == null) { // but they did not announce it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    result.setSilentXXICatch(-21);
                } else {
                    result.setSilentXXICatch(-63);
                }

            } else {
                result.setSilentXXICatch(21);
            }
        } else { // and they announced it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setXXICatchPayment(opponentXXICatch, result, -1);
                } else {
                    setXXICatchPayment(opponentXXICatch, result, -3);
                }
            } else {
                setXXICatchPayment(opponentXXICatch, result, 1);
            }
        }
    }

    private void handleVolatOpponentAchieved(Player player, RoundResult result, Bonus declarerVolat, Bonus opponentVolat, int bidMultiplier, Game game) {
        if (opponentVolat == null) { // Opponent did not announce it
            if (declarerVolat == null) { // Declarer also did not announce it
                if (player.getRoleInGame().getTeam().equals("declarer")) {
                    if (!game.isDeclarerAlone()) {
                        result.setSilentVolat(-3 * bidMultiplier);
                    } else {
                        result.setSilentVolat(-9 * bidMultiplier);
                    }
                } else {
                    result.setSilentVolat(3 * bidMultiplier);
                }
            } else { // Opponent has silent volat, but declarer announced it (very edge case)
                if (player.getRoleInGame().getTeam().equals("declarer")) {
                    if (!game.isDeclarerAlone()) {
                        setEdgeCaseVolatPayment(declarerVolat, result, bidMultiplier, -1);
                    } else {
                        setEdgeCaseVolatPayment(declarerVolat, result, bidMultiplier, -3);
                    }

                } else {
                    setEdgeCaseVolatPayment(declarerVolat, result, bidMultiplier, 1);
                }
            }
        } else { // Opponent announced it
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setNormalVolatPayment(declarerVolat, result, bidMultiplier, -1);
                } else {
                    setNormalVolatPayment(declarerVolat, result, bidMultiplier, -3);
                }
            } else {
                setNormalVolatPayment(declarerVolat, result, bidMultiplier, 1);
            }
        }
    }

    private void handleDoubleGameOpponentAchieved(Player player, RoundResult result, Bonus declarerDoubleGame, Bonus opponentDoubleGame, int cardCount, int bidMultiplier, Game game) {
        if (opponentDoubleGame != null) { // Opponent announced double game
            if (player.getRoleInGame().getTeam().equals("declarer")) {
                if (!game.isDeclarerAlone()) {
                    setDoubleGamePayment(opponentDoubleGame, result, bidMultiplier, -1);
                } else {
                    setDoubleGamePayment(opponentDoubleGame, result, bidMultiplier, -3);
                }

            } else {
                setDoubleGamePayment(opponentDoubleGame, result, bidMultiplier, 1);
            }
        } else { // Opponent did not announce double game
            if (declarerDoubleGame != null) { // Declarer announced double game (very edge case)
                if (player.getRoleInGame().getTeam().equals("declarer")) {
                    if (!game.isDeclarerAlone()) {
                        setDoubleGamePayment(declarerDoubleGame, result, bidMultiplier, -1);
                        if (cardCount > 0) {
                            result.setSilentDoubleGame(-declarerDoubleGame.getPointValue() * bidMultiplier);
                        }
                    } else {
                        setDoubleGamePayment(declarerDoubleGame, result, bidMultiplier, -3);
                        if (cardCount > 0) {
                            result.setSilentDoubleGame(-3 * declarerDoubleGame.getPointValue() * bidMultiplier);
                        }
                    }

                } else {
                    setDoubleGamePayment(declarerDoubleGame, result, bidMultiplier, 1);
                    if (cardCount > 0) {
                        result.setSilentDoubleGame(declarerDoubleGame.getPointValue() * bidMultiplier);
                    }
                }
            } else { // Neither side announced double game
                if (cardCount > 0) {
                    if (player.getRoleInGame().getTeam().equals("declarer")) {
                        if (!game.isDeclarerAlone()) {
                            result.setSilentDoubleGame(-4 * bidMultiplier);
                        } else {
                            result.setSilentDoubleGame(-12 * bidMultiplier);
                        }
                    } else {
                        result.setSilentDoubleGame(4 * bidMultiplier);
                    }
                }
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

    private void setUltimoPayment(Bonus bonus, RoundResult roundResult, int multiplier) {
        if (bonus.getLevel() == 0) {
            roundResult.setUltimo(multiplier * bonus.getPointValue());
        } else if (bonus.getLevel() == 1) {
            roundResult.setUltimoDoubled(multiplier * bonus.getPointValue());
        } else if (bonus.getLevel() == 2) {
            roundResult.setUltimoRedoubled(multiplier * bonus.getPointValue());
        }
    }

    private void setXXICatchPayment(Bonus bonus, RoundResult roundResult, int multiplier) {
        if (bonus.getLevel() == 0) {
            roundResult.setXXICatch(multiplier * bonus.getPointValue());
        } else if (bonus.getLevel() == 1) {
            roundResult.setXXICatchDoubled(multiplier * bonus.getPointValue());
        } else if (bonus.getLevel() == 2) {
            roundResult.setXXICatchRedoubled(multiplier * bonus.getPointValue());
        }
    }

    private void setNormalVolatPayment(Bonus bonus, RoundResult result, int bidMultiplier, int winnerMultiplier) {
        if (bonus.getLevel() == 0) {
            result.setVolat(bonus.getPointValue() * bidMultiplier * winnerMultiplier);
        } else if (bonus.getLevel() == 1) {
            result.setVolatDoubled(bonus.getPointValue() * bidMultiplier * winnerMultiplier);
        } else if (bonus.getLevel() == 2) {
            result.setVolatRedoubled(bonus.getPointValue() * bidMultiplier * winnerMultiplier);
        }
    }

    private void setEdgeCaseVolatPayment(Bonus bonus, RoundResult result, int bidMultiplier, int winnerMultiplier) {
        if (bonus.getLevel() == 0) {
            result.setVolat(-bonus.getPointValue() * bidMultiplier * winnerMultiplier);
            result.setSilentVolat(3 * bidMultiplier * winnerMultiplier);
        } else if (bonus.getLevel() == 1) {
            result.setVolatDoubled(bonus.getPointValue() * bidMultiplier * winnerMultiplier);
            result.setSilentVolat(3 * bidMultiplier * winnerMultiplier);
        } else if (bonus.getLevel() == 2) {
            result.setVolatRedoubled(bonus.getPointValue() * bidMultiplier * winnerMultiplier);
            result.setSilentVolat(3 * bidMultiplier * winnerMultiplier);
        }
    }

    private void setDoubleGamePayment(Bonus bonus, RoundResult result, int bidMultiplier, int winnerMultiplier) {
        if (bonus.getLevel() == 0) {
            result.setDoubleGame(bonus.getPointValue() * bidMultiplier * winnerMultiplier);
        } else if (bonus.getLevel() == 1) {
            result.setDoubleGameDoubled(bonus.getPointValue() * bidMultiplier * winnerMultiplier);
        } else if (bonus.getLevel() == 2) {
            result.setDoubleGameRedoubled(bonus.getPointValue() * bidMultiplier * winnerMultiplier);
        }
    }

    private void setHigherLevelPartyPayment(Bonus party, RoundResult result, int bidMultiplier, int winnerMultiplier) {
        if (party.getLevel() == 1) {
            result.setPartyDoubled(party.getPointValue() * bidMultiplier * winnerMultiplier);
        } else {
            result.setPartyRedoubled(party.getPointValue() * bidMultiplier * winnerMultiplier);
        }
    }
}
