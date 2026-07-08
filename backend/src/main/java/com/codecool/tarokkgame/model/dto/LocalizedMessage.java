package com.codecool.tarokkgame.model.dto;

import java.util.Map;

public record LocalizedMessage(String key, Map<String, Object> params) {

    public LocalizedMessage(String key) {
        this(key, Map.of());
    }
}
