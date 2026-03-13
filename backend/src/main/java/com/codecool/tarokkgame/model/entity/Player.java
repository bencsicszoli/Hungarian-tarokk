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

    @OneToMany(mappedBy="player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerCard> playerCards;

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
    private Set<BidLevel> bidLevels; // Is it really necessary?

    public String getName() {
        return user.getUsername();
    }
/*
    public void addCard(PlayerCard card) {
        cards.add(card);
    }

 */
    public List<PlayerCard> getSortedCards() {
        List<PlayerCard> sortedCards = new ArrayList<>(playerCards);
        sortedCards.sort(Comparator.comparingInt(o -> o.getCard().getId()));
        return sortedCards;
    }

    public int getBalance() {
        return user.getBalance();
    }

    public void setBalance(int balance) {
        user.setBalance(balance);
    }

    public boolean hasAnyHonours() {
        for (PlayerCard playerCard : playerCards) {
            if (playerCard.getCard().getStrength() == 1 ||
            playerCard.getCard().getStrength() == 21 ||
            playerCard.getCard().getStrength() == 22) {
                return true;
            }
        }
        return false;
    }

    public boolean couldAnnounce18Invit() {
        return checkNumberOfTarokksAndGivenTarokkExist(18);
    }

    public boolean couldAnnounce19Invit() {
        return checkNumberOfTarokksAndGivenTarokkExist(19);
    }

    private boolean checkNumberOfTarokksAndGivenTarokkExist(int tarokkStrength) {
        int tarokks = 0;
        boolean tarokkExist = false;
        for (PlayerCard playerCard : playerCards) {
            if (playerCard.getCard().getStrength() > 0) {
                tarokks++;
            }
            if (playerCard.getCard().getStrength() == tarokkStrength) {
                tarokkExist = true;
            }
        }
        return tarokkExist && tarokks >= 5;
    }
}

