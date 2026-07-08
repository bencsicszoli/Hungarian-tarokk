package com.codecool.tarokkgame.model.dto.messagedto.response;

import com.codecool.tarokkgame.model.dto.LocalizedMessage;

public record PublicSkartDTO(String username, int playerHandLength, int declarerSkartLength, int opponentSkartLength, LocalizedMessage discardedCardsInfo, String turnPlayer, String type) {
}
