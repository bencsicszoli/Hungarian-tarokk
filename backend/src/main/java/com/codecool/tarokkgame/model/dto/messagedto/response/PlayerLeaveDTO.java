package com.codecool.tarokkgame.model.dto.messagedto.response;

import com.codecool.tarokkgame.model.PlayerData;

import java.util.Map;

public record PlayerLeaveDTO(String playerName, String info, Map<String, PlayerData> playersData,  String type) {
}
