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

    int place;

    @ManyToOne
    private AppUser user;

    @ManyToOne
    private Game game;

    @OneToMany
    private List<PlayerCard> cards;

    private boolean eightTarokksInAdvance;
    private boolean eightTarokksAfterwards;
    private boolean nineTarokksInAdvance;
    private boolean nineTarokksAfterwards;

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
}

