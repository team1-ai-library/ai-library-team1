package com.nhnacademy.ailibraryteam1.common.util;

import com.pgvector.PGvector;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.SQLException;
import java.util.Objects;

@Converter
public class VectorConverter implements AttributeConverter<float[], String> {

    @Override
    public String convertToDatabaseColumn(float[] attribute) {
        if (Objects.nonNull(attribute)) {
            return new PGvector(attribute).toString(); // "[0.1, 0.2, ..]" 이런 형태
        }
        return null;
    }

    @Override
    public float[] convertToEntityAttribute(String dbData) {
        if (Objects.isNull(dbData)) {
            return null;
            // TODO embedding 필드에 널 제약 조건 걸게 되면 null 주면 안 되고 예외 던져야 함
        }

        try {
            return new PGvector(dbData).toArray();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}