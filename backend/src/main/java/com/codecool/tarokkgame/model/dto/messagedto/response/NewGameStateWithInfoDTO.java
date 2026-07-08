package com.codecool.tarokkgame.model.dto.messagedto.response;

import com.codecool.tarokkgame.model.dto.LocalizedMessage;

public record NewGameStateWithInfoDTO(String gameState, LocalizedMessage info, String type) {
}
