package com.codecool.tarokkgame.model.dto.messagedto;

import java.util.List;

public record DeclarerSkartWithTarokkDTO(List<PlayerCardDTO> cards, String info, String type) {}
