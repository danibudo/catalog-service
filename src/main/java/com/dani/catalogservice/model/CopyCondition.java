package com.dani.catalogservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum CopyCondition {

    GOOD("good"),
    DAMAGED("damaged");

    private final String value;

    CopyCondition(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CopyCondition fromValue(String value) {
        for (CopyCondition c : values()) {
            if (c.value.equals(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown copy condition: " + value);
    }

    @Converter(autoApply = true)
    public static class PersistenceConverter implements AttributeConverter<CopyCondition, String> {

        @Override
        public String convertToDatabaseColumn(CopyCondition attribute) {
            return attribute == null ? null : attribute.getValue();
        }

        @Override
        public CopyCondition convertToEntityAttribute(String dbData) {
            return dbData == null ? null : CopyCondition.fromValue(dbData);
        }
    }
}