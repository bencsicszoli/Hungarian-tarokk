package com.codecool.tarokkgame.model.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpecialBidCasesDTO {
    private boolean couldInviteWith18;
    private boolean couldInviteWith19;
    private boolean couldYieldWith20;

    public SpecialBidCasesDTO(boolean couldInviteWith18, boolean couldInviteWith19, boolean couldYieldWith20) {
        this.couldInviteWith18 = couldInviteWith18;
        this.couldInviteWith19 = couldInviteWith19;
        this.couldYieldWith20 = couldYieldWith20;
    }
}
