package com.codecool.tarokkgame.model.dto.restdto;

import java.util.List;

public record JwtResponseDTO(String jwt, String username, List<String> roles) {
}
