package com.codecool.tarokkgame.constants;

import com.codecool.tarokkgame.model.dto.LocalizedMessage;
import lombok.Getter;
import java.util.*;
import java.util.stream.Collectors;

public enum Bonus {
    PASS(0, 0, 1, "Pass", 1, MessageKey.BONUS_NAME_PASS),
    TRULL(1, 0, 16, "Trull", 2, MessageKey.BONUS_NAME_TRULL),
    FOUR_KINGS(2, 0, 17, "Four kings", 2, MessageKey.BONUS_NAME_FOUR_KINGS),
    DOUBLE(3, 0, 18, "Double game", 4, MessageKey.BONUS_NAME_DOUBLE_GAME),
    PAGAT_ULTIMO(4, 0, 19, "Pagat ultimo", 10, MessageKey.BONUS_NAME_PAGAT_ULTIMO),
    XXI_CATCH(5, 0, 20, "XXI-catch", 42, MessageKey.BONUS_NAME_XXI_CATCH),
    VOLAT(6, 0, 21, "Volat", 6, MessageKey.BONUS_NAME_VOLAT),
    PARTY_DOUBLED(0, 1, 3, "Party doubled", 2, MessageKey.BONUS_NAME_PARTY_DOUBLED),
    TRULL_DOUBLED(1, 1, 10, "Trull doubled", 4, MessageKey.BONUS_NAME_TRULL_DOUBLED),
    FOUR_KINGS_DOUBLED(2, 1, 11, "Four kings doubled", 4, MessageKey.BONUS_NAME_FOUR_KINGS_DOUBLED),
    DOUBLE_DOUBLED(3, 1, 12, "Double game doubled", 8, MessageKey.BONUS_NAME_DOUBLE_GAME_DOUBLED),
    PAGAT_ULTIMO_DOUBLED(4, 1, 13, "Pagat ultimo doubled", 20, MessageKey.BONUS_NAME_PAGAT_ULTIMO_DOUBLED),
    XXI_CATCH_DOUBLED(5, 1, 14, "XXI-catch doubled", 84, MessageKey.BONUS_NAME_XXI_CATCH_DOUBLED),
    VOLAT_DOUBLED(6, 1, 15, "Volat doubled", 12, MessageKey.BONUS_NAME_VOLAT_DOUBLED),
    PARTY_RE_DOUBLED(0, 2, 2, "Party re-doubled", 4, MessageKey.BONUS_NAME_PARTY_RE_DOUBLED),
    TRULL_RE_DOUBLED(1, 2, 4, "Trull re-doubled", 8, MessageKey.BONUS_NAME_TRULL_RE_DOUBLED),
    FOUR_KINGS_RE_DOUBLED(2, 2, 5, "Four kings re-doubled", 8, MessageKey.BONUS_NAME_FOUR_KINGS_RE_DOUBLED),
    DOUBLE_RE_DOUBLED(3, 2, 6, "Double game re-doubled", 16, MessageKey.BONUS_NAME_DOUBLE_GAME_RE_DOUBLED),
    PAGAT_ULTIMO_RE_DOUBLED(4, 2, 7, "Pagat ultimo re-doubled", 40, MessageKey.BONUS_NAME_PAGAT_ULTIMO_RE_DOUBLED),
    XXI_CATCH_RE_DOUBLED(5, 2, 8, "XXI-catch re-doubled", 168, MessageKey.BONUS_NAME_XXI_CATCH_RE_DOUBLED),
    VOLAT_RE_DOUBLED(6, 2, 9, "Volat re-doubled", 24, MessageKey.BONUS_NAME_VOLAT_RE_DOUBLED),;

    @Getter
    private final int bonusIndex;
    @Getter
    private final int level;
    private final int order;
    @Getter
    private final String bonusName;
    @Getter
    private final int pointValue;
    private final String nameKey;

    Bonus(int bonusIndex, int level, int order, String bonusName, int pointValue, String nameKey) {
        this.bonusIndex = bonusIndex;
        this.level = level;
        this.order = order;
        this.bonusName = bonusName;
        this.pointValue = pointValue;
        this.nameKey = nameKey;
    }

    public LocalizedMessage getLocalizedName() {
        return new LocalizedMessage(nameKey);
    }

    public LocalizedMessage getLocalizedSummaryName() {
        return this == PASS ? new LocalizedMessage(MessageKey.BONUS_NAME_PARTY) : getLocalizedName();
    }

    public static Bonus getBonusByIndexAndLevel(int bonusIndex, int level) {
        for (Bonus bonus : Bonus.values()) {
            if (bonus.bonusIndex == bonusIndex && bonus.level == level) {
                return bonus;
            }
        }
        throw new NoSuchElementException("There is no bonus with index " + bonusIndex + " and level " + level);
    }

    public static Bonus getBonusWithNextLevel(Bonus bonus) {
        if (bonus.level == 2) {
            throw new NoSuchElementException("The " + bonus.bonusName + " is already redoubled");
        }
        return getBonusByIndexAndLevel(bonus.bonusIndex, bonus.level + 1);
    }

    public static List<Bonus> getBonusesWithBaseLevel() {
        List<Bonus> bonuses = new ArrayList<>();
        for (Bonus bonus : Bonus.values()) {
            if (bonus.level == 0) {
                bonuses.add(bonus);
            }
        }
        return bonuses;
    }

    public static Bonus getBonusByName(String name) {
        for (Bonus bonus : Bonus.values()) {
            if (bonus.bonusName.equals(name)) {
                return bonus;
            }
        }
        throw new NoSuchElementException("There is no bonus with name " + name);
    }

    public static List<LocalizedMessage> getLocalizedBonusNames(Set<Bonus> bonuses) {
        List<Bonus> sortedBonuses = bonuses.stream().sorted(Comparator.comparingInt(b -> b.bonusIndex)).toList();
        return sortedBonuses.stream().map(Bonus::getLocalizedSummaryName).collect(Collectors.toList());
    }

    public static List<Bonus> sortBonuses(Set<Bonus> bonuses) {
        return bonuses.stream().sorted(Comparator.comparingInt(b -> b.order)).collect(Collectors.toList());
    }

    public static boolean isContainReDoubled(List<Bonus> bonuses) {
        return bonuses.stream().anyMatch(b -> b.level == 2);
    }

    public static boolean canBeFoundBasicLevelBonusInOpponentBonuses(Set<Bonus> opponentBonuses, List<Bonus> announcedBonuses) {
        boolean isBonusExist = false;
        for (Bonus ownBonus : announcedBonuses) {
            if (ownBonus.level == 1) {
                Bonus baseLevelBonus = getBonusByIndexAndLevel(ownBonus.bonusIndex, 0);
                boolean bonusFound = baseLevelBonus.isContainBonus(opponentBonuses);
                if (bonusFound) {
                    isBonusExist = true;
                }
            }
        }
        return isBonusExist;
    }

    private boolean isContainBonus(Set<Bonus> bonuses) {
        boolean isBonusExist = false;
        for (Bonus bonus : bonuses) {
            if (bonus.bonusIndex == bonusIndex && bonus.level == level) {
                isBonusExist = true;
                break;
            }
        }
        return isBonusExist;
    }
}
