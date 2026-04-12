package com.dani.catalogservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum CopyStatus {

    AVAILABLE("available"),
    ON_LOAN("on_loan"),
    DECOMMISSIONED("decommissioned");

    private final String value;

    CopyStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CopyStatus fromValue(String value) {
        for (CopyStatus s : values()) {
            if (s.value.equals(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown copy status: " + value);
    }

    @Converter(autoApply = true)
    public static class PersistenceConverter implements AttributeConverter<CopyStatus, String> {

        @Override
        public String convertToDatabaseColumn(CopyStatus attribute) {
            return attribute == null ? null : attribute.getValue();
        }

        @Override
        public CopyStatus convertToEntityAttribute(String dbData) {
            return dbData == null ? null : CopyStatus.fromValue(dbData);
        }
    }
}