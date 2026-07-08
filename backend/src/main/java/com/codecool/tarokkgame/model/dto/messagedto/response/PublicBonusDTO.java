package com.codecool.tarokkgame.model.dto.messagedto.response;

import com.codecool.tarokkgame.model.dto.LocalizedMessage;

import java.util.List;

public record PublicBonusDTO(List<LocalizedMessage> info, List<String> declarerBonuses, List<String> opponentBonuses, String turnPlayer, String type) {
}
