package com.codecool.tarokkgame.model.entity;

import com.codecool.tarokkgame.constants.BidLevel;
import com.codecool.tarokkgame.constants.RoleInGame;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Entity
@Getter
@Setter
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    int place = 0;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne
    @JoinColumn(name="game_id")
    private Game game;

    @OneToMany
    private List<PlayerCard> cards;

    private boolean eightTarokksInAdvance = false;
    private boolean eightTarokksAfterwards = false;
    private boolean nineTarokksInAdvance = false;
    private boolean nineTarokksAfterwards = false;

    @Enumerated(EnumType.STRING)
    private BidLevel bidLevel = BidLevel.NONE;

    @Enumerated(EnumType.STRING)
    private RoleInGame roleInGame = RoleInGame.NOT_CLEAR_YET;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "potential_bids", joinColumns = @JoinColumn(name = "player_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "bid")
    private Set<BidLevel> bidLevels;

    public String getName() {
        return user.getUsername();
    }
/*
    public void addCard(PlayerCard card) {
        cards.add(card);
    }

 */
    public List<PlayerCard> getSortedCards() {
        List<PlayerCard> sortedCards = new ArrayList<>(cards);
        sortedCards.sort(Comparator.comparingInt(o -> o.getCard().getId()));
        return sortedCards;
    }

    public int getBalance() {
        return user.getBalance();
    }

    public void setBalance(int balance) {
        user.setBalance(balance);
    }
}

