package com.codecool.tarokkgame.model.dto.messagedto;

import java.util.Set;

public record BonusesRequestDTO(String username, long gameId, int selectedTarokkNumber, Set<String> bonuses) {
}
