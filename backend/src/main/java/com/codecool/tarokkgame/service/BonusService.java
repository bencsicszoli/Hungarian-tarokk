package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.Bonus;
import com.codecool.tarokkgame.constants.GameState;
import com.codecool.tarokkgame.constants.RoleInGame;
import com.codecool.tarokkgame.constants.RomanTarokkNumber;
import com.codecool.tarokkgame.model.TarokkNumberHolder;
import com.codecool.tarokkgame.model.dto.messagedto.response.FirstPotentialBonusesDTO;
import com.codecool.tarokkgame.model.dto.messagedto.response.PotentialBonusesDTO;
import com.codecool.tarokkgame.model.dto.messagedto.response.PrivateInfoDTO;
import com.codecool.tarokkgame.model.dto.messagedto.response.PublicBonusDTO;
import com.codecool.tarokkgame.model.entity.Game;
import com.codecool.tarokkgame.model.entity.Player;
import com.codecool.tarokkgame.repository.GameRepository;
import com.codecool.tarokkgame.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BonusService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;

    public BonusService(GameRepository gameRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }

    public FirstPotentialBonusesDTO getFirstPotentialDeclarerBonuses(Game game, Player player) {
        List<Bonus> bonuses = Bonus.getBonusesWithBaseLevel();
        List<String> bonusNames = bonuses.stream().map(Bonus::getBonusName).collect(Collectors.toList());
        Set<String> callableTarokks = new LinkedHashSet<>();
        setCallableTarokks(game, player, callableTarokks);
        TarokkNumberHolder tarokkNumberHolder = getTarokkNumber(player);
        game.getOptionalBonuses().addAll(bonuses);
        game.getOptionalBonuses().addAll(bonuses);
        gameRepository.save(game);
        playerRepository.save(player);
        return new FirstPotentialBonusesDTO(tarokkNumberHolder.isEightTarokk(), tarokkNumberHolder.isNineTarokk(), bonusNames, callableTarokks, "game.firstPotentialBonuses");
    }

    public PotentialBonusesDTO getFirstPotentialTurnPlayerBonuses(Game game, Player player) {
        TarokkNumberHolder tarokkNumberHolder = getTarokkNumber(player);
        Set<Bonus> bonuses = game.getOptionalBonuses();
        //List<String> bonusNames = new ArrayList<>(bonuses.stream().map(Bonus::getBonusName).toList());
        String playerInfo = "";
        if (tarokkNumberHolder.isEightTarokk() || tarokkNumberHolder.isNineTarokk()) {
            playerInfo += "Announce tarokknumber (optional)! !";
        }
        if (player.getRoleInGame().equals(RoleInGame.DECLARER_PARTNER) || player.hasTheGivenTarokk(game.getInvitedTarokk())) {
            playerInfo += "Choose at least one bonus or pass! !";
        } else {
            if (player.getRoleInGame().equals(RoleInGame.OPPONENT)) {
                playerInfo += "Choose at least one bonus or pass!";
            } else {
                playerInfo += "Pass or choose at least one bonus after doubling something!";
            }
            Set<Bonus> declarerBonuses = game.getDeclarerBonuses();
            //List<String> namesOfDoubledBonuses = declarerBonuses.stream().map(bonus -> Bonus.getBonusWithNextLevel(bonus).getBonusName()).toList();
            bonuses.addAll(declarerBonuses);
        }
        List<Bonus> sortedBonuses = Bonus.sortBonuses(bonuses);
        List<String> bonusNames = sortedBonuses.stream().map(Bonus::getBonusName).toList();
        gameRepository.save(game);
        playerRepository.save(player);
        return new PotentialBonusesDTO(tarokkNumberHolder.isEightTarokk(), tarokkNumberHolder.isNineTarokk(), bonusNames, playerInfo, "game.firstTurnPlayerBonuses");
    }

    public PotentialBonusesDTO getPotentialTurnPlayerBonuses(Game game, Player player) {
        TarokkNumberHolder tarokkNumberHolder;
        if (!player.isEightTarokksInAdvance() && !player.isNineTarokksInAdvance()) {
            tarokkNumberHolder = getTarokkNumber(player);
        } else {
            tarokkNumberHolder = new TarokkNumberHolder();
        }
        String playerInfo = "";
        if (tarokkNumberHolder.isEightTarokk() || tarokkNumberHolder.isNineTarokk()) {
            playerInfo += "Announce tarokknumber (optional)! !";
        }
        playerInfo += "Choose at least one bonus or pass! !";
        Set<Bonus> optionalBonuses = game.getOptionalBonuses();
        Set<Bonus> bonuses = new HashSet<>(optionalBonuses);
        if (player.getRoleInGame().getTeam().equals("declarer")) {
            Set<Bonus> declarerBonuses = game.getDeclarerBonuses();
            for (Bonus bonus : declarerBonuses) {
                if (bonus.getLevel() == 1) {
                    Bonus redoubledBonus = Bonus.getBonusByIndexAndLevel(bonus.getBonusIndex(), 2);
                    bonuses.add(redoubledBonus);
                }
            }
            Set<Bonus> opponentBonuses = game.getOpponentBonuses();
            for (Bonus bonus : opponentBonuses) {
                if (bonus.getLevel() == 0) {
                    Bonus doubledBonus = Bonus.getBonusByIndexAndLevel(bonus.getBonusIndex(), 1);
                    bonuses.add(doubledBonus);
                }
            }
        } else {
            Set<Bonus> opponentBonuses = game.getOpponentBonuses();
            for (Bonus bonus : opponentBonuses) {
                if (bonus.getLevel() == 1) {
                    Bonus redoubledBonus = Bonus.getBonusByIndexAndLevel(bonus.getBonusIndex(), 2);
                    bonuses.add(redoubledBonus);
                }
            }
            Set<Bonus> declarerBonuses = game.getDeclarerBonuses();
            for (Bonus bonus : declarerBonuses) {
                if (bonus.getLevel() == 0) {
                    Bonus doubledBonus = Bonus.getBonusByIndexAndLevel(bonus.getBonusIndex(), 1);
                    bonuses.add(doubledBonus);
                }
            }
        }
        List<Bonus> sortedBonuses = Bonus.sortBonuses(bonuses);
        List<String> bonusNames = sortedBonuses.stream().map(Bonus::getBonusName).toList();
        gameRepository.save(game);
        playerRepository.save(player);
        return new PotentialBonusesDTO(tarokkNumberHolder.isEightTarokk(), tarokkNumberHolder.isNineTarokk(), bonusNames, playerInfo, "game.turnPlayerBonuses" );

        //List<String> bonusNames = new ArrayList<>(bonuses.stream().map(Bonus::getBonusName).toList());
    }

    public PublicBonusDTO getFirstPublicBonusInfo(Game game, Player player, Set<String> bonusNames, int selectedTarokkNumber, String calledTarokk) {
        switch (selectedTarokkNumber) {
            case 0: break;
            case 8: player.setEightTarokksInAdvance(true); break;
            case 9: player.setNineTarokksInAdvance(true); break;
            default: throw new RuntimeException("Invalid selected tarokk number: " + selectedTarokkNumber);
        }
        if (bonusNames.size() == 1 && bonusNames.contains("Pass")) {
            game.setBonusPasses(1); // Handle if bonusPasses == 3!!!
        } else {
            for (String bonusName : bonusNames) {
                Bonus bonus = Bonus.getBonusByName(bonusName);
                if (!bonusName.equals("Pass")) {
                    game.getOptionalBonuses().remove(bonus);
                }
                game.getDeclarerBonuses().add(bonus);
            }
        }
        String turnPlayer = game.getNextPlayer(player).getName();
        calledTarokk = calledTarokk.substring(11);
        System.out.println("Called Tarokk: " + calledTarokk);
        int invitedTarokkNumber = RomanTarokkNumber.toArabicNumber(calledTarokk);
        game.setInvitedTarokk(invitedTarokkNumber);
        String upperCaseName = player.getName().toUpperCase();
        String announceTarokkNumber = "";
        if (selectedTarokkNumber == 8) {
            announceTarokkNumber = upperCaseName + " has 8 tarokks! ";
        } else if (selectedTarokkNumber == 9) {
            announceTarokkNumber = upperCaseName + " has 9 tarokks! ";
        }
        String bonusNamesToString = String.join(", ", bonusNames);
        String info = announceTarokkNumber +
                upperCaseName +
                " called the " +
                calledTarokk +
                "! " +
                upperCaseName +
                " announced " +
                bonusNamesToString + "!";
        return new PublicBonusDTO(info, Bonus.getBonusNames(game.getDeclarerBonuses()), Bonus.getBonusNames(game.getOpponentBonuses()), turnPlayer, "game.publicBonusInfo");
    }

    public PublicBonusDTO getPublicBonusInfo(Game game, Player player, Set<String> bonusNames, int selectedTarokkNumber) {
        switch (selectedTarokkNumber) {
            case 0: break;
            case 8: player.setEightTarokksInAdvance(true); break;
            case 9: player.setNineTarokksInAdvance(true); break;
            default: throw new RuntimeException("Invalid selected tarokk number: " + selectedTarokkNumber);
        }
        for (String bonusName : bonusNames) {
            Bonus bonus = Bonus.getBonusByName(bonusName);
            if (!bonusName.equals("Pass") && bonus.getLevel() == 0) {
                game.getOptionalBonuses().remove(bonus);
                if (player.getRoleInGame().getTeam().equals("declarer")) {
                    game.getDeclarerBonuses().add(bonus);
                } else if (player.getRoleInGame().equals(RoleInGame.OPPONENT)) {
                    game.getOpponentBonuses().add(bonus);
                }
            } else if (bonus.getLevel() == 1) {
                if (player.getRoleInGame().getTeam().equals("declarer")) {
                    game.getOpponentBonuses().add(bonus);
                    Bonus opponentBonusToRemove = Bonus.getBonusByIndexAndLevel(bonus.getBonusIndex(), 0);
                    game.getOpponentBonuses().remove(opponentBonusToRemove);
                } else if (player.getRoleInGame().equals(RoleInGame.OPPONENT)) {
                    game.getDeclarerBonuses().add(bonus);
                    Bonus declarerBonusToRemove = Bonus.getBonusByIndexAndLevel(bonus.getBonusIndex(), 0);
                    game.getDeclarerBonuses().remove(declarerBonusToRemove);
                }
            } else { // bonus.getLevel() == 2
                if (player.getRoleInGame().getTeam().equals("declarer")) {
                    game.getDeclarerBonuses().add(bonus);
                    Bonus declarerBonusToRemove = Bonus.getBonusByIndexAndLevel(bonus.getBonusIndex(), 1);
                    game.getDeclarerBonuses().remove(declarerBonusToRemove);
                } else if (player.getRoleInGame().equals(RoleInGame.OPPONENT)) {
                    game.getOpponentBonuses().add(bonus);
                    Bonus opponentBonusToRemove = Bonus.getBonusByIndexAndLevel(bonus.getBonusIndex(), 1);
                    game.getOpponentBonuses().remove(opponentBonusToRemove);
                }
            }
            game.getDeclarerBonuses().add(bonus);
        }
        String turnPlayer = game.getNextPlayer(player).getName();
        String upperCaseName = player.getName().toUpperCase();
        String announceTarokkNumber = "";
        if (selectedTarokkNumber == 8) {
            announceTarokkNumber = upperCaseName + " has 8 tarokks! ";
        } else if (selectedTarokkNumber == 9) {
            announceTarokkNumber = upperCaseName + " has 9 tarokks! ";
        }
        String bonusNamesToString = String.join(", ", bonusNames);
        String info = announceTarokkNumber +
                upperCaseName +
                " announced " +
                bonusNamesToString + "!";
        return new PublicBonusDTO(info, Bonus.getBonusNames(game.getDeclarerBonuses()), Bonus.getBonusNames(game.getOpponentBonuses()), turnPlayer, "game.publicBonusInfo");
    }

    public PrivateInfoDTO checkDoubleOrRedoubleIfNeeded(Game game, Player player, Set<String> bonusNames) {
        List<Bonus> bonuses = bonusNames.stream().map(Bonus::getBonusByName).toList();
        if (bonusNames.size() == 1 && bonusNames.contains("Pass")) {
            game.setBonusPasses(game.getBonusPasses() + 1);
            if (game.getBonusPasses() == 3) {
                game.setState(GameState.TRICK_PHASE);
            }
            return null;
        } else {
            if (!player.getRoleInGame().equals(RoleInGame.NOT_CLEAR_YET)) {
                return null;
            } else {
                if (player.hasTheGivenTarokk(game.getInvitedTarokk())) {
                    player.setRoleInGame(RoleInGame.DECLARER_PARTNER);
                    // playerRepository.save(player)
                    game.markPlayersAsOpponent();
                    if (game.getLastBonusAnnouncer().equals("declarer")) {
                        return null;
                    } else {
                        Set<Bonus> opponentBonuses = game.getOpponentBonuses();
                        if (Bonus.isContainReDoubled(bonuses) || Bonus.canBeFoundBasicLevelBonusInOpponentBonuses(opponentBonuses, bonuses)) {
                            return null;
                        } else {
                            return new PrivateInfoDTO("You MUST redouble something otherwise it is not clear that you belong to the declarer", "game.privateInfo");
                        }
                    }
                } else {
                    player.setRoleInGame(RoleInGame.OPPONENT);
                    // playerRepository.save(player)
                    if (game.getNumberOfOpponents() == 2) {
                        game.setLastPlayerAsDeclarer();
                    }
                    if (game.getLastBonusAnnouncer().equals("opponent")) {
                        return null;
                    } else {
                        Set<Bonus> declarerBonuses = game.getDeclarerBonuses();
                        if (Bonus.isContainReDoubled(bonuses) || Bonus.canBeFoundBasicLevelBonusInOpponentBonuses(declarerBonuses, bonuses)) {
                            return null;
                        } else {
                            return new PrivateInfoDTO("You MUST double something otherwise it is not clear that you are an opponent", "game.privateInfo");
                        }
                    }
                }
            }
        }
    }

    public PrivateInfoDTO checkDeclarerUltimoAndTarokkNumber(Player player, Set<String> bonusNames, int selectedTarokkNumber) {
        if (bonusNames.contains("Pagat ultimo") && selectedTarokkNumber == 0) {
            if (player.isHasEightTarokks()) {
                return new PrivateInfoDTO("If you announce Pagat ultimo you MUST announce 8 tarokks too!", "game.privateInfo");
            } else if (player.isHasNineTarokks()) {
                return new PrivateInfoDTO("If you announce Pagat ultimo you MUST announce 9 tarokks too!", "game.privateInfo");
            } else {
                return null;
            }
        }
        return null;
    }

    public PrivateInfoDTO checkTurnPlayerUltimoAndTarokkNumber(Player player, Set<String> bonusNames, int selectedTarokkNumber) {
        if ((bonusNames.contains("Pagat ultimo") || bonusNames.contains("Double pagat ultimo")) && selectedTarokkNumber == 0) {
            if (player.isEightTarokksInAdvance() || player.isNineTarokksInAdvance()) {
                return null;
            }
            if (player.isHasEightTarokks()) {
                return new PrivateInfoDTO("If you announce or double Pagat ultimo you MUST announce 8 tarokks too!", "game.privateInfo");
            } else if (player.isHasNineTarokks()) {
                return new PrivateInfoDTO("If you announce or double Pagat ultimo you MUST announce 9 taroks too!", "game.privateInfo");
            } else {
                return null;
            }
        }
        return null;
    }

    private void setCallableTarokks(Game game, Player player, Set<String> callableTarokks) {
        if (game.getInvitedTarokk() == 20) {
            callableTarokks.add("I call the XX");
        } else if (game.getInvitedTarokk() == 19) {
            callableTarokks.add("I call the XIX");
        } else if (game.getInvitedTarokk() == 18) {
            callableTarokks.add("I call the XVIII");
        } else if (!player.hasTarokk20()) {
            callableTarokks.add("I call the XX");
        } else {
            int callableTarokk = player.findMissingStrongestTarokk();
            String romanForm = RomanTarokkNumber.fromArabicNumber(callableTarokk).toString();
            callableTarokks.add("I call the " + romanForm);
            callableTarokks.add("I call the XX");
        }
    }

    private TarokkNumberHolder getTarokkNumber(Player player) {
        TarokkNumberHolder tarokkNumberHolder = new TarokkNumberHolder();
        //int numberOfTarokks = player.getNumberOfTarokks();

        if (player.isHasEightTarokks()) {
            tarokkNumberHolder.setEightTarokk(true);
        } else if (player.isHasNineTarokks()) {
            tarokkNumberHolder.setNineTarokk(true);
        } else {
            int numberOfTarokks = player.getNumberOfTarokks();
            if (numberOfTarokks == 8) {
                tarokkNumberHolder.setEightTarokk(true);
                player.setHasEightTarokks(true);
            } else if (numberOfTarokks == 9) {
                tarokkNumberHolder.setNineTarokk(true);
                player.setHasNineTarokks(true);
            }
        }

        return tarokkNumberHolder;
    }
}
