package com.codecool.tarokkgame.model;

import com.codecool.tarokkgame.model.entity.Player;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TarokkNumberAnnouncers {
    private Set<Player> players;

    public TarokkNumberAnnouncers(Set<Player> players) {
        this.players = players;
    }
}
