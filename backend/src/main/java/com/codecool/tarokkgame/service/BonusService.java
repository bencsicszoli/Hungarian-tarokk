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
        gameRepository.save(game);
        playerRepository.save(player);
        return new FirstPotentialBonusesDTO(tarokkNumberHolder.isEightTarokk(), tarokkNumberHolder.isNineTarokk(), bonusNames, callableTarokks, "game.firstPotentialBonuses");
    }

    public PotentialBonusesDTO getFirstPotentialTurnPlayerBonuses(Game game, Player player) {
        TarokkNumberHolder tarokkNumberHolder = getTarokkNumber(player);
        Set<Bonus> optionalBonuses = game.getOptionalBonuses();
        Set<Bonus> bonuses = new HashSet<>(optionalBonuses);

        String playerInfo = createPlayerInfo(tarokkNumberHolder, player, game, bonuses);

        List<Bonus> sortedBonuses = Bonus.sortBonuses(bonuses);
        List<String> bonusNames = sortedBonuses.stream().map(Bonus::getBonusName).toList();
        gameRepository.save(game);
        playerRepository.save(player);
        return new PotentialBonusesDTO(tarokkNumberHolder.isEightTarokk(), tarokkNumberHolder.isNineTarokk(), bonusNames, playerInfo, "game.firstTurnPlayerBonuses");
    }

    public PotentialBonusesDTO getPotentialTurnPlayerBonuses(Game game, Player player) {
        TarokkNumberHolder tarokkNumberHolder = fillTarokkNumberHolder(player);
        String playerInfo = createPlayerInfo(tarokkNumberHolder);
        Set<Bonus> optionalBonuses = game.getOptionalBonuses();
        Set<Bonus> bonuses = new HashSet<>(optionalBonuses);

        setPotentialBonuses(player, bonuses, game);

        List<Bonus> sortedBonuses = Bonus.sortBonuses(bonuses);
        List<String> bonusNames = sortedBonuses.stream().map(Bonus::getBonusName).toList();
        gameRepository.save(game);
        playerRepository.save(player);
        return new PotentialBonusesDTO(tarokkNumberHolder.isEightTarokk(), tarokkNumberHolder.isNineTarokk(), bonusNames, playerInfo, "game.turnPlayerBonuses" );
    }

    public PublicBonusDTO getFirstPublicBonusInfo(Game game, Player player, Set<String> bonusNames, int selectedTarokkNumber, String calledTarokk) {
        setAnnouncedTarokkNumberToPlayer(selectedTarokkNumber, player);
        setNewBonusesToDeclarer(game, bonusNames, player);
        String turnPlayer = game.getNextPlayer(player).getName();
        calledTarokk = calledTarokk.substring(11);
        int invitedTarokkNumber = RomanTarokkNumber.toArabicNumber(calledTarokk);
        game.setInvitedTarokk(invitedTarokkNumber);
        String upperCaseName = player.getName().toUpperCase();
        String announceTarokkNumber = announceTarokkNumber(selectedTarokkNumber, upperCaseName);
        String bonusNamesToString = String.join(", ", bonusNames);
        String info = createInfoAboutDeclarerAnnouncement(announceTarokkNumber, upperCaseName, calledTarokk, bonusNamesToString);
        gameRepository.save(game);
        return new PublicBonusDTO(info, Bonus.getBonusNames(game.getDeclarerBonuses()), Bonus.getBonusNames(game.getOpponentBonuses()), turnPlayer, "game.publicBonusInfo");
    }

    public PublicBonusDTO getPublicBonusInfo(Game game, Player player, Set<String> bonusNames, int selectedTarokkNumber) {
        setAnnouncedTarokkNumberToPlayer(selectedTarokkNumber, player);
        if (bonusNames.size() > 1 || !bonusNames.contains("Pass")) {
            game.setBonusPasses(0);
        }
        for (String bonusName : bonusNames) {
            handlePlayerBonus(game, bonusName, player);
        }
        if (player.getRoleInGame().getTeam().equals("opponent")) {
            game.setLastBonusAnnouncer("opponent");
        } else if (player.getRoleInGame().getTeam().equals("declarer") && (bonusNames.size() > 1 || !bonusNames.contains("Pass"))) {
            game.setLastBonusAnnouncer("declarer");
        }
        String turnPlayer = game.getNextPlayer(player).getName();
        String upperCaseName = player.getName().toUpperCase();
        String info = createInfoAboutTurnPlayerAnnouncement(selectedTarokkNumber, upperCaseName, bonusNames);

        return new PublicBonusDTO(info, Bonus.getBonusNames(game.getDeclarerBonuses()), Bonus.getBonusNames(game.getOpponentBonuses()), turnPlayer, "game.publicBonusInfo");
    }

    public PrivateInfoDTO checkDoubleOrRedoubleIfNeeded(Game game, Player player, Set<String> bonusNames) {
        List<Bonus> bonuses = bonusNames.stream().map(Bonus::getBonusByName).toList();
        if (bonusNames.size() == 1 && bonusNames.contains("Pass")) {
            handleLonelyPass(game);
            return null;
        } else {
            if (!player.getRoleInGame().equals(RoleInGame.NOT_CLEAR_YET)) {
                return null;
            } else {
                if (player.hasTheGivenTarokk(game.getInvitedTarokk())) {
                    return checkNewDeclarerPartner(player, game, bonuses);
                } else {
                    return checkNewOpponent(player, game, bonuses);
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

    public PrivateInfoDTO checkDoubleGameAndVolat(Set<String> bonusNames) {
        if (bonusNames.contains("Double game") && bonusNames.contains("Volat")) {
            return new PrivateInfoDTO("You cannot announce Double game and Volat at the same time!", "game.privateInfo");
        }
        return null;
    }

    public PrivateInfoDTO checkAnnouncementsAfterVolat(Player player, Set<String> bonusNames, Game game) {
        String side = identifyPlayerSide(player, game.getInvitedTarokk());
        boolean announcedVolat = game.announcedVolat(side);
        if (announcedVolat && (bonusNames.contains("Trull") || bonusNames.contains("Four kings") || bonusNames.contains("Double game"))) {
            return new PrivateInfoDTO("You cannot announce Trull, Four kings or Double game after volat!", "game.privateInfo");
        } else {
            return null;
        }
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

    private String createPlayerInfo(TarokkNumberHolder tarokkNumberHolder, Player player, Game game, Set<Bonus> bonuses) {
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
            addDoubledBonusesToBonuses(game, bonuses);
        }
        return playerInfo;
    }

    private void addDoubledBonusesToBonuses(Game game, Set<Bonus> bonuses) {
        Set<Bonus> declarerBonuses = game.getDeclarerBonuses();
        Set<Bonus> doubledDeclarerBonuses = declarerBonuses.stream().map(Bonus::getBonusWithNextLevel).collect(Collectors.toSet());
        bonuses.addAll(doubledDeclarerBonuses);
    }

    private TarokkNumberHolder fillTarokkNumberHolder(Player player) {
        TarokkNumberHolder tarokkNumberHolder;
        if (!player.isEightTarokksInAdvance() && !player.isNineTarokksInAdvance()) {
            tarokkNumberHolder = getTarokkNumber(player);
        } else {
            tarokkNumberHolder = new TarokkNumberHolder();
        }
        return tarokkNumberHolder;
    }

    private String createPlayerInfo(TarokkNumberHolder tarokkNumberHolder) {
        String playerInfo = "";
        if (tarokkNumberHolder.isEightTarokk() || tarokkNumberHolder.isNineTarokk()) {
            playerInfo += "Announce tarokknumber (optional)! !";
        }
        playerInfo += "Choose at least one bonus or pass! !";
        return playerInfo;
    }

    private void setPotentialBonusesForDeclarerTeam(Game game, Set<Bonus> bonuses) {
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
    }

    private void setPotentialBonusesForOpponentTeam(Game game, Set<Bonus> bonuses) {
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

    private void setPotentialBonuses(Player player, Set<Bonus> bonuses, Game game) {
        if (player.getRoleInGame().getTeam().equals("declarer")) {
            setPotentialBonusesForDeclarerTeam(game, bonuses);
        } else if (player.getRoleInGame().getTeam().equals("opponent")) {
            setPotentialBonusesForOpponentTeam(game, bonuses);
        } else {
            boolean isDeclarerPartner = player.hasTheGivenTarokk(game.getInvitedTarokk());
            if (isDeclarerPartner) {
                setPotentialBonusesForDeclarerTeam(game, bonuses);

            } else { // Player does not have the called tarokk
                setPotentialBonusesForOpponentTeam(game, bonuses);
            }
        }
    }

    private void setAnnouncedTarokkNumberToPlayer(int selectedTarokkNumber, Player player) {
        switch (selectedTarokkNumber) {
            case 0: break;
            case 8: player.setEightTarokksInAdvance(true); break;
            case 9: player.setNineTarokksInAdvance(true); break;
            default: throw new RuntimeException("Invalid selected tarokk number: " + selectedTarokkNumber);
        }
    }

    private void setNewBonusesToDeclarer(Game game, Set<String> bonusNames, Player player) {
        if (!(bonusNames.size() == 1 && bonusNames.contains("Pass"))) {
            for (String bonusName : bonusNames) {
                Bonus bonus = Bonus.getBonusByName(bonusName);
                if (!bonus.equals(Bonus.PASS)) {
                    game.getOptionalBonuses().remove(bonus);
                    if (bonus.equals(Bonus.PAGAT_ULTIMO)) {
                        player.setAnnouncedUltimo(true);
                    }
                }
                game.getDeclarerBonuses().add(bonus);
            }
        }
        game.getDeclarerBonuses().add(Bonus.PASS);
    }

    private String announceTarokkNumber(int selectedTarokkNumber, String upperCaseName) {
        String announceTarokkNumber = "";
        if (selectedTarokkNumber == 8) {
            announceTarokkNumber = upperCaseName + " has 8 tarokks! ";
        } else if (selectedTarokkNumber == 9) {
            announceTarokkNumber = upperCaseName + " has 9 tarokks! ";
        }
        return announceTarokkNumber;
    }

    private String createInfoAboutDeclarerAnnouncement(String announceTarokkNumber, String upperCaseName, String calledTarokk, String bonusNamesToString) {
        return announceTarokkNumber +
                upperCaseName +
                " called the " +
                calledTarokk +
                "! " +
                upperCaseName +
                " announced " +
                bonusNamesToString + "!";
    }

    private void handleBaseLevelBonus(Game game, Bonus bonus, Player player) {
        game.getOptionalBonuses().remove(bonus);
        if (player.getRoleInGame().getTeam().equals("declarer")) {
            game.getDeclarerBonuses().add(bonus);
        } else if (player.getRoleInGame().equals(RoleInGame.OPPONENT)) {
            game.getOpponentBonuses().add(bonus);
        }
    }

    private void handleDoubledBonus(Game game, Bonus bonus, Player player) {
        if (player.getRoleInGame().getTeam().equals("declarer")) {
            game.getOpponentBonuses().add(bonus);
            Bonus opponentBonusToRemove = Bonus.getBonusByIndexAndLevel(bonus.getBonusIndex(), 0);
            game.getOpponentBonuses().remove(opponentBonusToRemove);
        } else if (player.getRoleInGame().equals(RoleInGame.OPPONENT)) {
            game.getDeclarerBonuses().add(bonus);
            Bonus declarerBonusToRemove = Bonus.getBonusByIndexAndLevel(bonus.getBonusIndex(), 0);
            game.getDeclarerBonuses().remove(declarerBonusToRemove);
        }
    }

    private void handleRedoubledBonus(Game game, Bonus bonus, Player player) {
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

    private String createInfoAboutTurnPlayerAnnouncement(int selectedTarokkNumber, String upperCaseName, Set<String> bonusNames) {
        String announceTarokkNumber = "";
        if (selectedTarokkNumber == 8) {
            announceTarokkNumber = upperCaseName + " has 8 tarokks! ";
        } else if (selectedTarokkNumber == 9) {
            announceTarokkNumber = upperCaseName + " has 9 tarokks! ";
        }
        String bonusNamesToString = String.join(", ", bonusNames);
        String infoToSend = announceTarokkNumber +
                upperCaseName +
                " announced " +
                bonusNamesToString + "!";
        return infoToSend;
    }

    private void handlePlayerBonus(Game game, String bonusName, Player player) {
        Bonus bonus = Bonus.getBonusByName(bonusName);
        if (!bonus.equals(Bonus.PASS) && bonus.getLevel() == 0) {
            handleBaseLevelBonus(game, bonus, player);
        } else if (bonus.getLevel() == 1) {
            handleDoubledBonus(game, bonus, player);
        } else if (bonus.getLevel() == 2) {
            handleRedoubledBonus(game, bonus, player);
        }
    }

    private void handleLonelyPass(Game game) {
        game.setBonusPasses(game.getBonusPasses() + 1);
        if (game.getBonusPasses() == 3) {
            game.setState(GameState.TRICK_PHASE);
        }
    }

    private PrivateInfoDTO checkNewDeclarerPartner(Player player, Game game, List<Bonus> bonuses) {

        if (game.getLastBonusAnnouncer().equals("declarer")) {
            player.setRoleInGame(RoleInGame.DECLARER_PARTNER);
            game.markPlayersAsOpponent();
            return null;
        } else {
            Set<Bonus> opponentBonuses = game.getOpponentBonuses();
            if (Bonus.isContainReDoubled(bonuses) || Bonus.canBeFoundBasicLevelBonusInOpponentBonuses(opponentBonuses, bonuses)) {
                player.setRoleInGame(RoleInGame.DECLARER_PARTNER);
                game.markPlayersAsOpponent();
                return null;
            } else {
                return new PrivateInfoDTO("You MUST double or redouble something otherwise it is not clear that you belong to the declarer", "game.privateInfo");
            }
        }
    }

    private PrivateInfoDTO checkNewOpponent(Player player, Game game, List<Bonus> bonuses) {

        if (game.getLastBonusAnnouncer().equals("opponent")) {
            player.setRoleInGame(RoleInGame.OPPONENT);
            if (game.getNumberOfOpponents() == 2) {
                game.setLastPlayerAsDeclarer();
            }
            return null;
        } else {
            Set<Bonus> declarerBonuses = game.getDeclarerBonuses();
            if (Bonus.isContainReDoubled(bonuses) || Bonus.canBeFoundBasicLevelBonusInOpponentBonuses(declarerBonuses, bonuses)) {
                player.setRoleInGame(RoleInGame.OPPONENT);
                if (game.getNumberOfOpponents() == 2) {
                    game.setLastPlayerAsDeclarer();
                }
                return null;
            } else {
                return new PrivateInfoDTO("You MUST double something otherwise it is not clear that you are an opponent", "game.privateInfo");
            }
        }
    }

    private String identifyPlayerSide(Player player, int calledTarokk) {
        if (player.getRoleInGame().getTeam().equals("declarer")) {
            return "declarer";
        } else if (player.getRoleInGame().getTeam().equals("opponent")) {
            return "opponent";
        } else if (player.hasTheGivenTarokk(calledTarokk)) {
            return "declarer";
        } else {
            return "opponent";
        }
    }
}
