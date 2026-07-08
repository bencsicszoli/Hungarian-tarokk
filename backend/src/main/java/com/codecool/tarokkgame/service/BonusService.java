package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.Bonus;
import com.codecool.tarokkgame.constants.GameState;
import com.codecool.tarokkgame.constants.MessageKey;
import com.codecool.tarokkgame.constants.RoleInGame;
import com.codecool.tarokkgame.constants.RomanTarokkNumber;
import com.codecool.tarokkgame.model.TarokkNumberHolder;
import com.codecool.tarokkgame.model.dto.LocalizedMessage;
import com.codecool.tarokkgame.model.dto.messagedto.response.FirstPotentialBonusesDTO;
import com.codecool.tarokkgame.model.dto.messagedto.response.PotentialBonusesDTO;
import com.codecool.tarokkgame.model.dto.messagedto.response.PrivateInfoListDTO;
import com.codecool.tarokkgame.model.dto.messagedto.response.PublicBonusDTO;
import com.codecool.tarokkgame.model.entity.Game;
import com.codecool.tarokkgame.model.entity.Player;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BonusService {

    public BonusService() {
    }

    public FirstPotentialBonusesDTO getFirstPotentialDeclarerBonuses(Game game, Player player) {
        List<Bonus> bonuses = Bonus.getBonusesWithBaseLevel();
        List<String> bonusNames = bonuses.stream().map(Bonus::getBonusName).collect(Collectors.toList());
        Set<LocalizedMessage> callableTarokks = new LinkedHashSet<>();
        setCallableTarokks(game, player, callableTarokks);
        TarokkNumberHolder tarokkNumberHolder = getTarokkNumber(player);
        List<LocalizedMessage> playerInfo = createPlayerInfo(tarokkNumberHolder);
        game.getOptionalBonuses().addAll(bonuses);
        return new FirstPotentialBonusesDTO(tarokkNumberHolder.isEightTarokk(), tarokkNumberHolder.isNineTarokk(), bonusNames, callableTarokks, playerInfo, "game.firstPotentialBonuses");
    }

    public PotentialBonusesDTO getFirstPotentialTurnPlayerBonuses(Game game, Player player) {
        TarokkNumberHolder tarokkNumberHolder = getTarokkNumber(player);
        Set<Bonus> optionalBonuses = game.getOptionalBonuses();
        Set<Bonus> bonuses = new HashSet<>(optionalBonuses);

        List<LocalizedMessage> playerInfo = createPlayerInfo(tarokkNumberHolder, player, game, bonuses);

        List<Bonus> sortedBonuses = Bonus.sortBonuses(bonuses);
        List<String> bonusNames = sortedBonuses.stream().map(Bonus::getBonusName).toList();
        return new PotentialBonusesDTO(tarokkNumberHolder.isEightTarokk(), tarokkNumberHolder.isNineTarokk(), bonusNames, playerInfo, "game.firstTurnPlayerBonuses");
    }

    public PotentialBonusesDTO getPotentialTurnPlayerBonuses(Game game, Player player) {
        TarokkNumberHolder tarokkNumberHolder = fillTarokkNumberHolder(player);
        List<LocalizedMessage> playerInfo = createPlayerInfo(tarokkNumberHolder);
        Set<Bonus> optionalBonuses = game.getOptionalBonuses();
        Set<Bonus> bonuses = new HashSet<>(optionalBonuses);

        setPotentialBonuses(player, bonuses, game);

        List<Bonus> sortedBonuses = Bonus.sortBonuses(bonuses);
        List<String> bonusNames = sortedBonuses.stream().map(Bonus::getBonusName).toList();
        return new PotentialBonusesDTO(tarokkNumberHolder.isEightTarokk(), tarokkNumberHolder.isNineTarokk(), bonusNames, playerInfo, "game.turnPlayerBonuses" );
    }

    public PublicBonusDTO getFirstPublicBonusInfo(Game game, Player player, Set<String> bonusNames, int selectedTarokkNumber, String calledTarokk) {
        setAnnouncedTarokkNumberToPlayer(selectedTarokkNumber, player);
        setNewBonusesToDeclarer(game, bonusNames, player);
        String turnPlayer = game.getNextPlayer(player).getName();
        int invitedTarokkNumber = RomanTarokkNumber.toArabicNumber(calledTarokk);
        game.setInvitedTarokk(invitedTarokkNumber);
        if (player.hasTheGivenTarokk(invitedTarokkNumber)) {
            game.setDeclarerAlone(true);
        }
        String upperCaseName = player.getName().toUpperCase();
        List<LocalizedMessage> info = createInfoAboutDeclarerAnnouncement(selectedTarokkNumber, upperCaseName, calledTarokk, bonusNames);
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
        List<LocalizedMessage> info = createInfoAboutTurnPlayerAnnouncement(selectedTarokkNumber, upperCaseName, bonusNames);

        return new PublicBonusDTO(info, Bonus.getBonusNames(game.getDeclarerBonuses()), Bonus.getBonusNames(game.getOpponentBonuses()), turnPlayer, "game.publicBonusInfo");
    }

    public PrivateInfoListDTO checkDoubleOrRedoubleIfNeeded(Game game, Player player, Set<String> bonusNames) {
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

    public PrivateInfoListDTO checkDeclarerUltimoAndTarokkNumber(Player player, Set<String> bonusNames, int selectedTarokkNumber) {
        if (bonusNames.contains("Pagat ultimo") && selectedTarokkNumber == 0) {
            if (player.isHasEightTarokks()) {
                return new PrivateInfoListDTO(List.of(new LocalizedMessage(MessageKey.BONUS_PAGAT_ULTIMO_REQUIRES_8)), "game.privateInfo");
            } else if (player.isHasNineTarokks()) {
                return new PrivateInfoListDTO(List.of(new LocalizedMessage(MessageKey.BONUS_PAGAT_ULTIMO_REQUIRES_9)), "game.privateInfo");
            } else {
                return null;
            }
        }
        return null;
    }

    public PrivateInfoListDTO checkTurnPlayerUltimoAndTarokkNumber(Player player, Set<String> bonusNames, int selectedTarokkNumber) {
        if ((bonusNames.contains("Pagat ultimo") || bonusNames.contains("Pagat ultimo doubled")) && selectedTarokkNumber == 0) {
            if (player.isEightTarokksInAdvance() || player.isNineTarokksInAdvance()) {
                return null;
            }
            if (player.isHasEightTarokks()) {
                return new PrivateInfoListDTO(List.of(new LocalizedMessage(MessageKey.BONUS_TURN_PAGAT_ULTIMO_REQUIRES_8)), "game.privateInfo");
            } else if (player.isHasNineTarokks()) {
                return new PrivateInfoListDTO(List.of(new LocalizedMessage(MessageKey.BONUS_TURN_PAGAT_ULTIMO_REQUIRES_9)), "game.privateInfo");
            } else {
                return null;
            }
        }
        return null;
    }

    public PrivateInfoListDTO checkDoubleGameAndVolat(Set<String> bonusNames) {
        if (bonusNames.contains("Double game") && bonusNames.contains("Volat")) {
            return new PrivateInfoListDTO(List.of(new LocalizedMessage(MessageKey.BONUS_DOUBLE_VOLAT_CONFLICT)), "game.privateInfo");
        }
        return null;
    }

    public PrivateInfoListDTO checkAnnouncementsAfterVolat(Player player, Set<String> bonusNames, Game game) {
        String side = identifyPlayerSide(player, game.getInvitedTarokk());
        boolean announcedVolat = game.announcedVolat(side);
        if (announcedVolat && (bonusNames.contains("Trull") || bonusNames.contains("Four kings") || bonusNames.contains("Double game"))) {
            return new PrivateInfoListDTO(List.of(new LocalizedMessage(MessageKey.BONUS_ANNOUNCE_AFTER_VOLAT_CONFLICT)), "game.privateInfo");
        } else {
            return null;
        }
    }

    public PrivateInfoListDTO informPlayerOfSelectedOptions(int selectedTarokkNumber, String calledTarokk, List<String> selectedBonuses) {
        List<LocalizedMessage> lines = new ArrayList<>();
        if (selectedTarokkNumber > 0) {
            lines.add(new LocalizedMessage(MessageKey.BONUS_SELECTED_TAROKKS, Map.of("count", selectedTarokkNumber)));
        }
        if (calledTarokk != null) {
            lines.add(callTarokkMessage(calledTarokk));
        }
        if (!selectedBonuses.isEmpty()) {
            String key = selectedBonuses.size() == 1 ? MessageKey.BONUS_SELECTED_BONUS : MessageKey.BONUS_SELECTED_BONUSES;
            lines.add(new LocalizedMessage(key, Map.of("bonuses", selectedBonuses)));
        }
        return new PrivateInfoListDTO(lines, "game.privateInfo");
    }

    private void setCallableTarokks(Game game, Player player, Set<LocalizedMessage> callableTarokks) {
        if (game.getInvitedTarokk() == 20) {
            callableTarokks.add(callTarokkMessage("XX"));
        } else if (game.getInvitedTarokk() == 19) {
            callableTarokks.add(callTarokkMessage("XIX"));
        } else if (game.getInvitedTarokk() == 18) {
            callableTarokks.add(callTarokkMessage("XVIII"));
        } else if (!player.hasTarokk20()) {
            callableTarokks.add(callTarokkMessage("XX"));
        } else {
            int callableTarokk = player.findMissingStrongestTarokk();
            String romanForm = RomanTarokkNumber.fromArabicNumber(callableTarokk).toString();
            callableTarokks.add(callTarokkMessage(romanForm));
            callableTarokks.add(callTarokkMessage("XX"));
        }
    }

    private LocalizedMessage callTarokkMessage(String romanNumeral) {
        return new LocalizedMessage(MessageKey.BONUS_CALL_TAROKK, Map.of("romanNumeral", romanNumeral));
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

    private List<LocalizedMessage> createPlayerInfo(TarokkNumberHolder tarokkNumberHolder, Player player, Game game, Set<Bonus> bonuses) {
        List<LocalizedMessage> playerInfo = new ArrayList<>();
        if (tarokkNumberHolder.isEightTarokk() || tarokkNumberHolder.isNineTarokk()) {
            playerInfo.add(new LocalizedMessage(MessageKey.BONUS_ANNOUNCE_TAROKK_PROMPT));
        }
        if (player.getRoleInGame().equals(RoleInGame.DECLARER_PARTNER) || player.hasTheGivenTarokk(game.getInvitedTarokk())) {
            playerInfo.add(new LocalizedMessage(MessageKey.BONUS_CHOOSE_PROMPT));
        } else {
            if (player.getRoleInGame().equals(RoleInGame.OPPONENT)) {
                playerInfo.add(new LocalizedMessage(MessageKey.BONUS_CHOOSE_PROMPT));
            } else {
                playerInfo.add(new LocalizedMessage(MessageKey.BONUS_PASS_OR_CHOOSE_PROMPT));
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

    private List<LocalizedMessage> createPlayerInfo(TarokkNumberHolder tarokkNumberHolder) {
        List<LocalizedMessage> playerInfo = new ArrayList<>();
        if (tarokkNumberHolder.isEightTarokk() || tarokkNumberHolder.isNineTarokk()) {
            playerInfo.add(new LocalizedMessage(MessageKey.BONUS_ANNOUNCE_TAROKK_PROMPT));
        }
        playerInfo.add(new LocalizedMessage(MessageKey.BONUS_CHOOSE_PROMPT));
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
                    setBonusAnnouncer(bonus, player, game, "declarer");
                }
                game.getDeclarerBonuses().add(bonus);
            }
        }
        game.getDeclarerBonuses().add(Bonus.PASS);
    }

    private void addTarokkNumberAnnouncementIfAny(int selectedTarokkNumber, String upperCaseName, List<LocalizedMessage> lines) {
        if (selectedTarokkNumber == 8 || selectedTarokkNumber == 9) {
            lines.add(new LocalizedMessage(MessageKey.BONUS_PLAYER_HAS_TAROKKS, Map.of("player", upperCaseName, "count", selectedTarokkNumber)));
        }
    }

    private List<LocalizedMessage> createInfoAboutDeclarerAnnouncement(int selectedTarokkNumber, String upperCaseName, String calledTarokk, Set<String> bonusNames) {
        List<LocalizedMessage> lines = new ArrayList<>();
        addTarokkNumberAnnouncementIfAny(selectedTarokkNumber, upperCaseName, lines);
        lines.add(new LocalizedMessage(MessageKey.BONUS_PLAYER_CALLED_TAROKK, Map.of("player", upperCaseName, "tarokk", calledTarokk)));
        lines.add(new LocalizedMessage(MessageKey.BONUS_PLAYER_ANNOUNCED, Map.of("player", upperCaseName, "bonuses", List.copyOf(bonusNames))));
        return lines;
    }

    private void handleBaseLevelBonus(Game game, Bonus bonus, Player player) {
        game.getOptionalBonuses().remove(bonus);
        if (player.getRoleInGame().getTeam().equals("declarer")) {
            game.getDeclarerBonuses().add(bonus);
            setBonusAnnouncer(bonus, player, game, "declarer");
        } else if (player.getRoleInGame().equals(RoleInGame.OPPONENT)) {
            game.getOpponentBonuses().add(bonus);
            setBonusAnnouncer(bonus, player, game, "opponent");
        }
    }

    private void setBonusAnnouncer(Bonus bonus, Player player, Game game, String announcerTeam) {
        if (bonus.equals(Bonus.PAGAT_ULTIMO)) {
            player.setAnnouncedUltimo(true);
        } else if (bonus.equals(Bonus.TRULL)) {
            game.setTrullAnnouncer(announcerTeam);
        } else if (bonus.equals(Bonus.FOUR_KINGS)) {
            game.setFourKingsAnnouncer(announcerTeam);
        } else if (bonus.equals(Bonus.DOUBLE)) {
            game.setDoubleGameAnnouncer(announcerTeam);
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

    private List<LocalizedMessage> createInfoAboutTurnPlayerAnnouncement(int selectedTarokkNumber, String upperCaseName, Set<String> bonusNames) {
        List<LocalizedMessage> lines = new ArrayList<>();
        addTarokkNumberAnnouncementIfAny(selectedTarokkNumber, upperCaseName, lines);
        lines.add(new LocalizedMessage(MessageKey.BONUS_PLAYER_ANNOUNCED, Map.of("player", upperCaseName, "bonuses", List.copyOf(bonusNames))));
        return lines;
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

    private PrivateInfoListDTO checkNewDeclarerPartner(Player player, Game game, List<Bonus> bonuses) {

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
                return new PrivateInfoListDTO(List.of(new LocalizedMessage(MessageKey.BONUS_MUST_DOUBLE_DECLARER_SIDE)), "game.privateInfo");
            }
        }
    }

    private PrivateInfoListDTO checkNewOpponent(Player player, Game game, List<Bonus> bonuses) {

        if (game.getLastBonusAnnouncer().equals("opponent")) {
            player.setRoleInGame(RoleInGame.OPPONENT);
            return null;
        } else {
            Set<Bonus> declarerBonuses = game.getDeclarerBonuses();
            if (Bonus.isContainReDoubled(bonuses) || Bonus.canBeFoundBasicLevelBonusInOpponentBonuses(declarerBonuses, bonuses)) {
                player.setRoleInGame(RoleInGame.OPPONENT);
                return null;
            } else {
                return new PrivateInfoListDTO(List.of(new LocalizedMessage(MessageKey.BONUS_MUST_DOUBLE_OPPONENT_SIDE)), "game.privateInfo");
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
