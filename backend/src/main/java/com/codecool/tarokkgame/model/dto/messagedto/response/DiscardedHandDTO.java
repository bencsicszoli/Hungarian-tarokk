package com.codecool.tarokkgame.model.dto.messagedto.response;

import com.codecool.tarokkgame.model.dto.LocalizedMessage;

import java.util.List;

public record DiscardedHandDTO(
        List<PlayerCardDTO> cards,
        String turnPlayer,
        List<LocalizedMessage> info,
        String type
    ) {
}
