package com.codecool.tarokkgame.model.dto.messagedto.response;

import com.codecool.tarokkgame.model.dto.LocalizedMessage;

import java.util.List;

public record PotentialBonusesDTO(boolean hasEightTarokks, boolean hasNineTarokks, List<String> bonuses, List<LocalizedMessage> info, String type) {
}
