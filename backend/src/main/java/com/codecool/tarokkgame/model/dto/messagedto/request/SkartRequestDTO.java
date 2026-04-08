package com.codecool.tarokkgame.model.dto.messagedto.request;

import com.codecool.tarokkgame.model.dto.messagedto.response.PlayerCardDTO;

import java.util.List;

public record SkartRequestDTO(String username, long gameId, List<PlayerCardDTO> cardsToSkart) {
}
