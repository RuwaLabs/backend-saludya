package com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.converters;

import com.ruwalabs.saludya.appointments.domain.model.valueobjects.AppointmentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts {@link AppointmentStatus} to and from its database string column.
 */
@Converter
public class AppointmentStatusAttributeConverter implements AttributeConverter<AppointmentStatus, String> {

    @Override
    public String convertToDatabaseColumn(AppointmentStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public AppointmentStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AppointmentStatus.valueOf(dbData);
    }
}
