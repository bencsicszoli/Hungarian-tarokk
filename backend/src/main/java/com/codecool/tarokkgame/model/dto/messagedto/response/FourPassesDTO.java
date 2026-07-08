package com.codecool.tarokkgame.model.dto.messagedto.response;

import com.codecool.tarokkgame.model.dto.LocalizedMessage;

import java.util.List;

public record FourPassesDTO(List<LocalizedMessage> info, String turnPlayer, String type) {
}
