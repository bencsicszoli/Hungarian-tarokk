package com.codecool.tarokkgame.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    String suit;
    String name;
    int pointValue;
    int strength;
    String frontImagePath;
}
