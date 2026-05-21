package com.codecool.tarokkgame.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "fake_deck")

public class FakeDeckCard {

    @Id
    private int id;

    String suit;
    String name;
    int pointValue;
    int strength;
    String frontImagePath;
}