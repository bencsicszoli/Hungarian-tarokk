package com.codecool.tarokkgame.model.dto.messagedto.response;

import com.codecool.tarokkgame.model.PlayerData;
import com.codecool.tarokkgame.model.dto.LocalizedMessage;

import java.util.Map;

public record PlayerLeaveDTO(String playerName, LocalizedMessage info, Map<String, PlayerData> playersData,  String type) {
}
