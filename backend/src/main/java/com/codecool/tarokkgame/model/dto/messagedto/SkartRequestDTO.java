package com.codecool.tarokkgame.model.dto.messagedto;

import java.util.List;

public record SkartRequestDTO(String username, long gameId, List<PlayerCardDTO> cardsToSkart) {
}
