package com.codecool.tarokkgame.model.dto.messagedto.request;

import java.util.List;

public record BonusInfoRequestDTO(String turnPlayer, long gameId, int selectedTarokkNumber, String calledTarokk, List<String> bonuses) {
}
