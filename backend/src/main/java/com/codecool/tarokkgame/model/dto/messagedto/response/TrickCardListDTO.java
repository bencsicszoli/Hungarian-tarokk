package com.codecool.tarokkgame.model.dto.messagedto.response;

import java.util.List;

public record TrickCardListDTO(List<TrickCardDTO> cards, String turnName, int cardsInHand, String type) {
}
