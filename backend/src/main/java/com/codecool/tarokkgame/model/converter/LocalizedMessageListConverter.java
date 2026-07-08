package com.codecool.tarokkgame.model.converter;

import com.codecool.tarokkgame.model.dto.LocalizedMessage;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Converter
public class LocalizedMessageListConverter implements AttributeConverter<List<LocalizedMessage>, String> {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();
    private static final TypeReference<List<LocalizedMessage>> LIST_TYPE = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<LocalizedMessage> attribute) {
        return attribute == null ? null : MAPPER.writeValueAsString(attribute);
    }

    @Override
    public List<LocalizedMessage> convertToEntityAttribute(String dbData) {
        return dbData == null ? null : MAPPER.readValue(dbData, LIST_TYPE);
    }
}
