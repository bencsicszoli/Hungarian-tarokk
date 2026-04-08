package com.codecool.tarokkgame.model.dto.messagedto.response;

import java.util.List;

public record DeclarerSkartWithTarokkDTO(List<PlayerCardDTO> cards, String info, String type) {}
