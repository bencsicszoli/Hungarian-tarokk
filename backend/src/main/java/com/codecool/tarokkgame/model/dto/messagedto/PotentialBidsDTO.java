package com.codecool.tarokkgame.model.dto.messagedto;

import java.util.List;

public record PotentialBidsDTO(String bidLevel, List<String> potentialBids, String type) {
}
