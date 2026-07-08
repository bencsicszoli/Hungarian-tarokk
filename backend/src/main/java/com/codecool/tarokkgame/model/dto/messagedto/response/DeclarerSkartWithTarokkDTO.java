package com.codecool.tarokkgame.model.dto.messagedto.response;

import com.codecool.tarokkgame.model.dto.LocalizedMessage;

import java.util.List;

public record DeclarerSkartWithTarokkDTO(List<PlayerCardDTO> cards, List<LocalizedMessage> info, String type) {}
