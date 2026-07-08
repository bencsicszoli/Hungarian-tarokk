package com.codecool.tarokkgame.model.dto.messagedto.response;

import com.codecool.tarokkgame.model.dto.LocalizedMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
    private List<LocalizedMessage> information;
    private List<LocalizedMessage> privateInformation;
    private String type;
    private int player1Balance;
    private int player2Balance;
    private int player3Balance;
    private int player4Balance;
    private String gameState = "NEW";
    private String levelDescription = "None";

    public JoinMessageDTO() {}
}
