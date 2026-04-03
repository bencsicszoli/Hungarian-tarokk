package com.codecool.tarokkgame.model.dto.messagedto;

import java.util.Set;

public record FirstDeclarerBonusesRequestDTO(String declarer, long gameId, int selectedTarokkNumber, String calledTarokk, Set<String> bonuses) {
}
