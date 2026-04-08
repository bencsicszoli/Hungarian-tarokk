package com.codecool.tarokkgame.model.dto.messagedto.response;

import java.util.Set;

public record PotentialBidsDTO(Set<String> potentialBids, String type) {
}
