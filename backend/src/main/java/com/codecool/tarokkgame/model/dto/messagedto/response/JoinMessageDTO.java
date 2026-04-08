package com.codecool.tarokkgame.model.dto.messagedto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinMessageDTO {

    private long gameId;
    private String player1;
    private String player2;
    private String player3;
    private String player4;
    private String dealer;
    private String startPlayer;
    private String turnPlayer;
    private String information;
    private String type;
    private int player1Balance;
    private int player2Balance;
    private int player3Balance;
    private int player4Balance;
    private String gameState = "NEW";
    private String levelDescription = "None";

    public JoinMessageDTO() {}
}
