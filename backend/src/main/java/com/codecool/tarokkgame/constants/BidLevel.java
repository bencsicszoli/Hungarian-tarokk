package com.codecool.tarokkgame.constants;

import lombok.Getter;

public enum BidLevel {
    NONE(0, 0, "None", null),
    PASS(0, 0, "Pass", null),
    THREE(1, 1, "3", new int[]{3, 1, 1, 1}),
    TWO(2, 2, "2", new int[]{2, 2, 1, 1}),
    TWO_HELD(2, 3, "Hold (2)", new int[]{2, 2, 1, 1}),
    ONE(3, 4, "1", new int[]{1, 2, 2, 1}),
    ONE_HELD(3, 5, "Hold (1)", new int[]{1, 2, 2, 1}),
    SOLO(4, 6, "Solo", new int[]{0, 2, 2, 2}),
    SOLO_HELD(4, 7, "Hold (solo)", new int[]{0, 2, 2, 2});

    @Getter
    final int bidValue;
    @Getter
    final int grade;
    @Getter
    final String description;
    @Getter
    final int[] cardsFromTalon;

    BidLevel(int bidValue, int grade, String description, int[] cardsFromTalon) {
        this.bidValue = bidValue;
        this.grade = grade;
        this.description = description;
        this.cardsFromTalon = cardsFromTalon;
    }

    public BidLevel getNextLevelAtFirstBid(BidLevel actualLevel, int step) {
        if (actualLevel.getBidValue() + step > 4) {
            return PASS;
        }
        return getBidLevelByBidValue(actualLevel.bidValue + step);
    }

    public BidLevel getNextLevelAtOtherBids(BidLevel actualLevel, int step) {
        if (actualLevel.grade + step > 7) {
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

    private BidLevel getBidLevelByGrade(int grade) {
        for (BidLevel bidLevel : BidLevel.values()) {
            if (grade == bidLevel.grade) {
                return bidLevel;
            }
        }
        throw new IllegalArgumentException("Invalid grade value: " + grade);
    }

    public static BidLevel getLevelByDescription(String description) {
        for (BidLevel bidLevel : BidLevel.values()) {
            if (description.equals(bidLevel.description)) {
                return bidLevel;
            }
        }
        throw new IllegalArgumentException("Invalid description: " + description);
    }

    public String getBidNameToDisplay() {
        return switch (bidValue) {
            case 0 -> "-";
            case 1 -> "3";
            case 2 -> "2";
            case 3 -> "1";
            case 4 -> "solo";
            default -> "Bid name not found";
        };
    }
}
