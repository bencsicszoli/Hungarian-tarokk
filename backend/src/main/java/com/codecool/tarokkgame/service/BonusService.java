package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.Bonus;
import com.codecool.tarokkgame.constants.RoleInGame;
import com.codecool.tarokkgame.constants.RomanTarokkNumber;
import com.codecool.tarokkgame.model.TarokkNumberHolder;
import com.codecool.tarokkgame.model.dto.messagedto.FirstPotentialBonusesDTO;
import com.codecool.tarokkgame.model.dto.messagedto.PotentialBonusesDTO;
import com.codecool.tarokkgame.model.dto.messagedto.PrivateInfoDTO;
import com.codecool.tarokkgame.model.dto.messagedto.PublicBonusDTO;
import com.codecool.tarokkgame.model.entity.Game;
import com.codecool.tarokkgame.model.entity.Player;
import com.codecool.tarokkgame.repository.GameRepository;
import com.codecool.tarokkgame.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
        List<String> bonusNames = new ArrayList<>(bonuses.stream().map(Bonus::getBonusName).toList());
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
            List<String> namesOfDoubledBonuses = declarerBonuses.stream().map(bonus -> Bonus.getBonusWithNextLevel(bonus).getBonusName()).toList();
            bonusNames.addAll(namesOfDoubledBonuses);
        }
        gameRepository.save(game);
        playerRepository.save(player);
        return new PotentialBonusesDTO(tarokkNumberHolder.isEightTarokk(), tarokkNumberHolder.isNineTarokk(), bonusNames, playerInfo, "game.firstTurnPlayerBonuses");
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
        String builder = announceTarokkNumber +
                upperCaseName +
                " called the " +
                calledTarokk +
                "! " +
                upperCaseName +
                " announced " +
                bonusNamesToString + "!";
        return new PublicBonusDTO(builder, Bonus.getBonusNames(game.getDeclarerBonuses()), Bonus.getBonusNames(game.getOpponentBonuses()), turnPlayer, "game.publicBonusInfo");
    }

    public PrivateInfoDTO checkUltimoAndTarokkNumber(Player player, Set<String> bonusNames, int selectedTarokkNumber) {
        if (bonusNames.contains("Pagat ultimo") && selectedTarokkNumber == 0) {
            if (player.isHasEightTarokks()) {
                return new PrivateInfoDTO("If you announce Pagat ultimo you MUST announce 8 tarokks too!", "game.ultimoCheck");
            } else if (player.isHasNineTarokks()) {
                return new PrivateInfoDTO("If you announce Pagat ultimo you MUST announce 9 tarokks too!", "game.ultimoCheck");
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
        int numberOfTarokks = player.getNumberOfTarokks();
        boolean hasEightTarokks = false;
        boolean hasNineTarokks = false;

        if (numberOfTarokks == 8) {
            hasEightTarokks = true;
            player.setHasEightTarokks(true);
        } else if (numberOfTarokks == 9) {
            hasNineTarokks = true;
            player.setHasNineTarokks(true);
        }
        return new TarokkNumberHolder(hasEightTarokks, hasNineTarokks);
    }
}
