package com.codecool.tarokkgame.model.dto.messagedto.response;

public record PublicSkartDTO(String username, int playerHandLength, int declarerSkartLength, int opponentSkartLength, String discardedCardsInfo, String turnPlayer, String type) {
}
