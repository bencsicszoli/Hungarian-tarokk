package com.codecool.tarokkgame.constants;

import lombok.Getter;

public enum RoleInGame {
    DECLARER("declarer"),
    DECLARER_PARTNER("declarer"),
    OPPONENT("opponent"),
    NOT_CLEAR_YET("none");

    @Getter
    private final String team;

    RoleInGame(String team) {
        this.team = team;
    }
}
