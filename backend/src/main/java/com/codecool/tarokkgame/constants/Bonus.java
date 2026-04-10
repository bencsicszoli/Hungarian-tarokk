package com.codecool.tarokkgame.constants;

import lombok.Getter;
import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;

public enum Bonus {
    PASS(0, 0, 1, "Pass"),
    TRULL(1, 0, 16, "Trull"),
    FOUR_KINGS(2, 0, 17, "Four kings"),
    DOUBLE(3, 0, 18, "Double game"),
    PAGAT_ULTIMO(4, 0, 19, "Pagat ultimo"),
    XXI_CATCH(5, 0, 20, "XXI-catch"),
    VOLAT(6, 0, 21, "Volat"),
    PARTY_DOUBLED(0, 1, 3, "Double party"),
    TRULL_DOUBLED(1, 1, 10, "Double trull"),
    FOUR_KINGS_DOUBLED(2, 1, 11, "Double four kings"),
    DOUBLE_DOUBLED(3, 1, 12, "Double double game"),
    PAGAT_ULTIMO_DOUBLED(4, 1, 13, "Double pagat ultimo"),
    XXI_CATCH_DOUBLED(5, 1, 14, "Double XXI-catch"),
    VOLAT_DOUBLED(6, 1, 15, "Double volat"),
    PARTY_RE_DOUBLED(0, 2, 2, "Redouble party"),
    TRULL_RE_DOUBLED(1, 2, 4, "Redouble trull"),
    FOUR_KINGS_RE_DOUBLED(2, 2, 5, "Redouble four kings"),
    DOUBLE_RE_DOUBLED(3, 2, 6, "Redouble double game"),
    PAGAT_ULTIMO_RE_DOUBLED(4, 2, 7, "Redouble pagat ultimo"),
    XXI_CATCH_RE_DOUBLED(5, 2, 8, "Redouble XXI-catch"),
    VOLAT_RE_DOUBLED(6, 2, 9, "Redouble volat");

    @Getter
    private final int bonusIndex;
    @Getter
    private final int level;
    private final int order;
    @Getter
    private final String bonusName;

    Bonus(int bonusIndex, int level, int order, String bonusName) {
        this.bonusIndex = bonusIndex;
        this.level = level;
        this.order = order;
        this.bonusName = bonusName;
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

    public static List<String> getBonusNames(Set<Bonus> bonuses) {
        List<String> bonusNames = new ArrayList<>();
        for (Bonus bonus : bonuses) {
            bonusNames.add(bonus.bonusName);
        }
        return bonusNames;
    }

    public static List<Bonus> sortBonuses(Set<Bonus> bonuses) {
        return bonuses.stream().sorted(Comparator.comparingInt(b -> b.order)).collect(Collectors.toList());
    }

    public static boolean isContainDoubled(List<Bonus> bonuses) {
        return bonuses.stream().anyMatch(b -> b.level == 1);
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
