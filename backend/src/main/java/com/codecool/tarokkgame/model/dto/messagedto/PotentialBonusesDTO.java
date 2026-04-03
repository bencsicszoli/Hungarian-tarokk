package com.codecool.tarokkgame.model.dto.messagedto;

import java.util.List;

public record PotentialBonusesDTO(boolean hasEightTarokks, boolean hasNineTarokks, List<String> bonuses, String info, String type) {
}
