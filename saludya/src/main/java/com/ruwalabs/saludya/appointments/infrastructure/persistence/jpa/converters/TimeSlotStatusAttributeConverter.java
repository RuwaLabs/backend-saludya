package com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.converters;

import com.ruwalabs.saludya.appointments.domain.model.valueobjects.TimeSlotStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts {@link TimeSlotStatus} to and from its database string column.
 */
@Converter
public class TimeSlotStatusAttributeConverter implements AttributeConverter<TimeSlotStatus, String> {

    @Override
    public String convertToDatabaseColumn(TimeSlotStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public TimeSlotStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TimeSlotStatus.valueOf(dbData);
    }
}
