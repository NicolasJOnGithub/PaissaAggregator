package com.paissa.aggregator.housing;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PlotSizeConverter implements AttributeConverter<PlotSize, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PlotSize attribute) {
        return attribute == null ? null : attribute.code();
    }

    @Override
    public PlotSize convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : PlotSize.fromCode(dbData);
    }
}
