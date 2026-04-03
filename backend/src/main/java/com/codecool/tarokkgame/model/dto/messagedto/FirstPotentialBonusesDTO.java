package com.codecool.tarokkgame.model.dto.messagedto;

import com.codecool.tarokkgame.constants.Bonus;

import java.util.List;
import java.util.Set;

public record FirstPotentialBonusesDTO(boolean hasEightTarokks, boolean hasNineTarokks, List<String> bonuses, Set<String> callableTarokks, String type) {
}
