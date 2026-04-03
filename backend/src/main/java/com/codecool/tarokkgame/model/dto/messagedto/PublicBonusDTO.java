package com.codecool.tarokkgame.model.dto.messagedto;

import java.util.List;

public record PublicBonusDTO(String info, List<String> declarerBonuses, List<String> opponentBonuses, String turnPlayer, String type) {
}
