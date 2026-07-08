package com.codecool.tarokkgame.model.dto;

import java.util.Map;

public record ErrorMessage(String key, Map<String, Object> params) {

    public static ErrorMessage from(LocalizedMessage message) {
        return new ErrorMessage(message.key(), message.params());
    }
}
