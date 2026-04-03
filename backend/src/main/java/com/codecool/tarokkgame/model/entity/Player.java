package com.codecool.tarokkgame.model.entity;

import com.codecool.tarokkgame.constants.BidLevel;
import com.codecool.tarokkgame.constants.RoleInGame;
import com.codecool.tarokkgame.model.dto.SpecialBidCasesDTO;
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
    int tarokksInSkart = 0;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne
    @JoinColumn(name="game_id")
    private Game game;

    @OneToMany(mappedBy="player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerCard> playerCards;

    private boolean announcedXVIII_Invit = false;
    private boolean announcedXIX_Invit = false;
    private boolean acceptedXVIII_Invit = false;
    private boolean acceptedXIX_Invit = false;
    private boolean yieldedGame = false;
    private boolean hasEightTarokks = false;
    private boolean hasNineTarokks = false;
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

    public SpecialBidCasesDTO getSpecialBidCases() {
        int tarokks = 0;
        boolean hasTarokk18 = false;
        boolean hasTarokk19 = false;
        boolean hasTarokk20 = false;
        for (PlayerCard playerCard : playerCards) {
            if (playerCard.getCard().getStrength() > 0) {
                tarokks++;
            }
            if (playerCard.getCard().getStrength() == 18) {
                hasTarokk18 = true;
            } else if (playerCard.getCard().getStrength() == 19) {
                hasTarokk19 = true;
            } else if (playerCard.getCard().getStrength() == 20) {
                hasTarokk20 = true;
            }
        }
        return new SpecialBidCasesDTO(
                tarokks > 4 && hasTarokk18,
                tarokks > 4 && hasTarokk19,
                tarokks > 4 && hasTarokk20
        );
    }

    public boolean hasTarokk20() {
        boolean hasTarokk20 = false;
        for (PlayerCard playerCard : playerCards) {
            if (playerCard.getCard().getStrength() == 20) {
                hasTarokk20 = true;
                break;
            }
        }
        return hasTarokk20;
    }

    public int findMissingStrongestTarokk() {
        Set<Integer> invalidTarokkStrengths = Set.of(20, 21, 22);
        int strongestTarokkPlayerHas = 0;
        for (PlayerCard playerCard : playerCards) {
            int cardStrength = playerCard.getCard().getStrength();
            if (cardStrength > strongestTarokkPlayerHas && !invalidTarokkStrengths.contains(cardStrength)) {
                strongestTarokkPlayerHas = cardStrength;
            }
        }
        return strongestTarokkPlayerHas - 1;
    }

    public int getNumberOfTarokks() {
        int tarokks = 0;
        for (PlayerCard playerCard : playerCards) {
            if (playerCard.getCard().getSuit().equals("tarokk")) {
                tarokks++;
            }
        }
        return tarokks;
    }

    public boolean hasTheGivenTarokk(int tarokkStrength) {
        for (PlayerCard playerCard : playerCards) {
            if (playerCard.getCard().getStrength() == tarokkStrength) {
                return true;
            }
        }
        return false;
    }
}

