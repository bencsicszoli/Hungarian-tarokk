package com.codecool.tarokkgame.model.dto.messagedto.response;

import java.util.List;

public record PlayerCardListDTO(List<PlayerCardDTO> cards, String type) {
}
