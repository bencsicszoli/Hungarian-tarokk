package com.codecool.tarokkgame.model.dto.messagedto.response;

import java.util.List;

public record DiscardedHandDTO(List<PlayerCardDTO> cards, String playerName, String turnPlayer, String info, String type) {
}
