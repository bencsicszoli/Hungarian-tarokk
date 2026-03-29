package com.codecool.tarokkgame.model.dto.messagedto;

public record PublicSkartDTO(String username, int playerHandLength, int declarerSkartLength, int opponentSkartLength, String discardedCardsInfo, String turnPlayer, String type) {
}
