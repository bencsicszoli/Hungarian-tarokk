package com.codecool.tarokkgame.model.dto.messagedto.response;

import com.codecool.tarokkgame.model.dto.LocalizedMessage;

public record NewRoundDTO(LocalizedMessage dealerInfo, String type) {
}
