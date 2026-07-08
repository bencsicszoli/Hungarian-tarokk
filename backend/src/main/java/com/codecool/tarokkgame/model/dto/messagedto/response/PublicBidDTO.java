package com.codecool.tarokkgame.model.dto.messagedto.response;

import com.codecool.tarokkgame.model.dto.LocalizedMessage;

public record PublicBidDTO(String declarer, String bidPlayer, String turnPlayer, String bid, LocalizedMessage info, String type) {
}
