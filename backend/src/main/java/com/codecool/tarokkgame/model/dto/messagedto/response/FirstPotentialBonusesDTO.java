package com.codecool.tarokkgame.model.dto.messagedto.response;

import com.codecool.tarokkgame.model.dto.LocalizedMessage;

import java.util.List;
import java.util.Set;

public record FirstPotentialBonusesDTO(boolean hasEightTarokks, boolean hasNineTarokks, List<BonusOptionDTO> bonuses, Set<LocalizedMessage> callableTarokks, List<LocalizedMessage> info, String type) {
}
