package com.codecool.backend.constants;

public enum RoleInGame {
    DECLARER("declarer"), DECLARER_PARTNER("declarer"), OPPONENT("opponent"), NOT_CLEAR_YET("none");

    private final String team;

    RoleInGame(String team) {
        this.team = team;
    }

    public Boolean isDeclarer(RoleInGame role) {
        return role.team.equals("declarer");
    }
    public Boolean isOpponent(RoleInGame role) {
        return role.team.equals("opponent");
    }
}
