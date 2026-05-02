package com.codecool.tarokkgame.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class RoundResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int party = 0;
    private int partyDoubled = 0;
    private int partyRedoubled = 0;
    private int silentTrull = 0;
    private int trull = 0;
    private int trullDoubled = 0;
    private int trullRedoubled = 0;
    private int silentFourKings = 0;
    private int fourKings = 0;
    private int fourKingsDoubled = 0;
    private int fourKingsRedoubled = 0;
    private int silentDoubleGame = 0;
    private int doubleGame = 0;
    private int doubleGameDoubled = 0;
    private int doubleGameRedoubled = 0;
    private int silentUltimo = 0;
    private int ultimo = 0;
    private int ultimoDoubled = 0;
    private int ultimoRedoubled = 0;
    private int silentXXICatch = 0;
    private int XXICatch = 0;
    private int XXICatchDoubled = 0;
    private int XXICatchRedoubled = 0;
    private int silentVolat = 0;
    private int volat = 0;
    private int volatDoubled = 0;
    private int volatRedoubled = 0;
    private int eightTarokksInAdvance = 0;
    private int nineTarokksInAdvance = 0;
    private int eightTarokksAfterwards = 0;
    private int nineTarokksAfterwards = 0;

    @OneToOne
    private Player player;
}
