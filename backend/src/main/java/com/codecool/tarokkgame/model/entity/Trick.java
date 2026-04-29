package com.codecool.tarokkgame.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Trick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    int x = 0;
    int y = 0;
    int rotation = 0;

    @ManyToOne
    private Player player;

    @ManyToOne
    private Card card;
}
