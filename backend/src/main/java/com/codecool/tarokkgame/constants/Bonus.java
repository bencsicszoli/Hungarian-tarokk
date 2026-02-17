package com.codecool.tarokkgame.constants;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public enum Bonus {
    PASS(0, 0, "Pass"),
    TRULL(1, 0, "Trull"),
    FOUR_KINGS(2, 0, "Four kings"),
    DOUBLE(3, 0, "Double game"),
    PAGAT_ULTIMO(4, 0, "Pagat ultimo"),
    XXI_CATCH(5, 0, "XXI-catch"),
    VOLAT(6, 0, "Volat"),
    PASS_DOUBLED(0, 1, "Double pass"),
    TRULL_DOUBLED(1, 1, "Double trull"),
    FOUR_KINGS_DOUBLED(2, 1, "Double four kings"),
    DOUBLE_DOUBLED(3, 1, "Double double game"),
    PAGAT_ULTIMO_DOUBLED(4, 1, "Double pagat ultimo"),
    XXI_CATCH_DOUBLED(5, 1, "Double XXI-catch"),
    VOLAT_DOUBLED(6, 1, "Double volat"),
    PASS_RE_DOUBLED(0, 2, "Redouble pass"),
    TRULL_RE_DOUBLED(1, 2, "Redouble trull"),
    FOUR_KINGS_RE_DOUBLED(2, 2, "Redouble four kings"),
    DOUBLE_RE_DOUBLED(3, 2, "Redouble double game"),
    PAGAT_ULTIMO_RE_DOUBLED(4, 2, "Redouble pagat ultimo"),
    XXI_CATCH_RE_DOUBLED(5, 2, "Redouble XXI-catch"),
    VOLAT_RE_DOUBLED(6, 2, "Redouble volat");

    private final int bonusIndex;
    private final int level;
    private final String bonusName;

    Bonus(int bonusIndex, int level, String bonusName) {
        this.bonusIndex = bonusIndex;
        this.level = level;
        this.bonusName = bonusName;
    }

    public Bonus getBonusByIndexAndLevel(int bonusIndex, int level) {
        for (Bonus bonus : Bonus.values()) {
            if (bonus.bonusIndex == bonusIndex && bonus.level == level) {
                return bonus;
            }
        }
        throw new NoSuchElementException("There is no bonus with index " + bonusIndex + " and level " + level);
    }

    public Bonus getBonusWithNextLevel(Bonus bonus) {
        if (bonus.level == 2) {
            throw new NoSuchElementException("The " + bonus.bonusName + " is already redoubled");
        }
        return getBonusByIndexAndLevel(bonus.bonusIndex, bonus.level + 1);
    }

    public List<Bonus> getBonusesWithBaseLevel() {
        List<Bonus> bonuses = new ArrayList<>();
        for (Bonus bonus : Bonus.values()) {
            if (bonus.level == 0) {
                bonuses.add(bonus);
            }
        }
        return bonuses;
    }

    public Bonus getBonusByName(String name) {
        for (Bonus bonus : Bonus.values()) {
            if (bonus.bonusName.equals(name)) {
                return bonus;
            }
        }
        throw new NoSuchElementException("There is no bonus with name " + name);
    }
}
