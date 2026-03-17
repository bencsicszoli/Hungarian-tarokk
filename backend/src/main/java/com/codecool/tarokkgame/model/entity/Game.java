package com.codecool.tarokkgame.model.entity;

import com.codecool.tarokkgame.constants.BidLevel;
import com.codecool.tarokkgame.constants.Bonus;
import com.codecool.tarokkgame.constants.GameLevel;
import com.codecool.tarokkgame.constants.GameState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Entity
@Getter
@Setter
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String player1;
    private String player2;
    private String player3;
    private String player4;

    private boolean isXIXInvit = false;
    private boolean isXVIIIInvit = false;
    private String invitAcceptor = null;
    private int biddingPasses = 0;
    private int invitedTarokk = 0;
    private int bonusPasses = 0;
    private String dealer;
    private String startPlayer;
    private String turnPlayer;
    private String declarer;
    private String information;
    private int cardOrder = 1;

    @OneToMany(mappedBy = "game")
    private List<Player> players;

    @Enumerated(EnumType.STRING)
    private GameLevel gameLevel = GameLevel.PASKIEVICS;

    @Enumerated(EnumType.STRING)
    private BidLevel bidLevel = BidLevel.NONE;

    @Enumerated(EnumType.STRING)
    private GameState state = GameState.NEW;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "declarer_bonuses", joinColumns = @JoinColumn(name = "game_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "bonus")
    private Set<Bonus> declarerBonuses;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "opponent_bonuses", joinColumns = @JoinColumn(name = "game_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "bonus")
    private Set<Bonus> opponentBonuses;

    public Player getNextPlayer(Player player) {
        if (player.getPlace() == 4) {
            for (Player activePlayer : players) {
                if (activePlayer.getPlace() == 1) {
                    return activePlayer;
                }
            }
        } else {
            for (Player activePlayer : players) {
                if (activePlayer.getPlace() == player.getPlace() + 1) {
                    return activePlayer;
                }
            }
        }
        throw new NoSuchElementException("Next player not found");
    }

    public Player getPlayerByName(String name) {
        for (Player player : players) {
            if (player.getUser().getUsername().equals(name)) {
                return player;
            }
        }
        throw new NoSuchElementException("Player " + name + " not found in the game");
    }

    public Player getNextBiddingPlayer(Player player) {
        int[] places = {0, 1, 2, 3, 4, 1, 2, 3};
        int playerIndex = places[player.getPlace()];
        for (int i = playerIndex + 1; i <= playerIndex + 3; i++) {
            for (Player biddingPlayer : players) {
                if (biddingPlayer.getPlace() == places[i] && biddingPlayer.getBidLevel() != BidLevel.PASS) {
                    return biddingPlayer;
                }
            }
        }
        return null;
    }


}
