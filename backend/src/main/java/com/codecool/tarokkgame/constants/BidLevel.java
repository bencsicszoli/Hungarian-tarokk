package com.codecool.tarokkgame.constants;

import lombok.Getter;

public enum BidLevel {
    NONE(0, 0, "None"),
    PASS(0, 0, "Pass"),
    THREE(1, 1, "Three"),
    TWO(2, 2, "Two"),
    TWO_HELD(2, 3, "Hold (2)"),
    ONE(3, 4, "One"),
    ONE_HELD(3, 5, "Hold (1)"),
    SOLO(4, 6, "Solo"),
    SOLO_HELD(4, 7, "Hold (solo)");

    final int bidValue;
    final int grade;
    @Getter
    final String description;

    BidLevel(int bidValue, int grade, String description) {
        this.bidValue = bidValue;
        this.grade = grade;
        this.description = description;
    }

    public BidLevel getNextLevelAtFirstBid(BidLevel actualLevel, int step) {
        if (actualLevel == SOLO) {
            return PASS;
        }
        return getBidLevelByBidValue(actualLevel.bidValue + step);
    }

    public BidLevel getNextLevelAtOtherBids(BidLevel actualLevel, int step) {
        if (actualLevel.grade == 7) {
            return PASS;
        }
        return getBidLevelByGrade(actualLevel.grade + step);
    }

    public BidLevel getBidLevelByBidValue(int bidValue) {
        for (BidLevel bidLevel : BidLevel.values()) {
            if (bidLevel.bidValue == bidValue) {
                return bidLevel;
            }
        }
        throw new IllegalArgumentException("Invalid bid value: " + bidValue);
    }

    public BidLevel getBidLevelByGrade(int grade) {
        for (BidLevel bidLevel : BidLevel.values()) {
            if (grade == bidLevel.grade) {
                return bidLevel;
            }
        }
        throw new IllegalArgumentException("Invalid grade value: " + grade);
    }

    public BidLevel getLevelByDescription(String description) {
        for (BidLevel bidLevel : BidLevel.values()) {
            if (description.equals(bidLevel.description)) {
                return bidLevel;
            }
        }
        throw new IllegalArgumentException("Invalid description: " + description);
    }

}
