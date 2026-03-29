package com.codecool.tarokkgame.model.entity;

import com.codecool.tarokkgame.constants.*;
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
    private boolean isYielded = false;
    private boolean isTarokkInDeclarerSkart = false;
    private boolean isTarokkInOpponentSkart = false;

    private String invitAcceptor;
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

    public void setPlayerRolesInCaseYieldedGameOrInvit(Player declarer) {
        if (isYielded) {
            setInvitedTarokk(20);
            for (Player player : players) {
                if (player.isYieldedGame()) {
                    player.setRoleInGame(RoleInGame.DECLARER_PARTNER);
                } else if (player.getRoleInGame() != RoleInGame.DECLARER) {
                    player.setRoleInGame(RoleInGame.OPPONENT);
                }
            }
        } else if (isXIXInvit) {
            if (declarer.isAcceptedXIX_Invit()) {
                setInvitedTarokk(19);
                for (Player player : players) {
                    if (player.isAnnouncedXIX_Invit()) {
                        player.setRoleInGame(RoleInGame.DECLARER_PARTNER);
                    } else if (player.getRoleInGame() != RoleInGame.DECLARER) {
                        player.setRoleInGame(RoleInGame.OPPONENT);
                    }
                }
            }
        } else if (isXVIIIInvit) {
            if (declarer.isAcceptedXVIII_Invit()) {
                setInvitedTarokk(18);
                for (Player player : players) {
                    if (player.isAnnouncedXVIII_Invit()) {
                        player.setRoleInGame(RoleInGame.DECLARER_PARTNER);
                    } else if (player.getRoleInGame() != RoleInGame.DECLARER) {
                        player.setRoleInGame(RoleInGame.OPPONENT);
                    }
                }
            }
        }
    }

    public String getMessageWithTarokksInOpponentSkart() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Player player : players) {
            if (player.getRoleInGame() != RoleInGame.DECLARER) {
                if (player.getTarokksInSkart() == 1) {
                    stringBuilder.append(player.getName()).append(" placed ").append(player.getTarokksInSkart()).append(" tarokk in skart!");
                } else if (player.getTarokksInSkart() == 2) {
                    stringBuilder.append(player.getName()).append(" placed ").append(player.getTarokksInSkart()).append(" tarokks in skart!");
                }
            }
        }
        return stringBuilder.toString();
    }
}
