package com.codecool.tarokkgame.model.converter;

import com.codecool.tarokkgame.model.dto.LocalizedMessage;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Converter
public class LocalizedMessageConverter implements AttributeConverter<LocalizedMessage, String> {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    @Override
    public String convertToDatabaseColumn(LocalizedMessage attribute) {
        return attribute == null ? null : MAPPER.writeValueAsString(attribute);
    }

    @Override
    public LocalizedMessage convertToEntityAttribute(String dbData) {
        return dbData == null ? null : MAPPER.readValue(dbData, LocalizedMessage.class);
    }
}
