package com.codecool.backend.model.entity;

import com.codecool.backend.constants.BidLevel;
import com.codecool.backend.constants.RoleInGame;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    int place;

    @ManyToOne
    private User user;

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
}

