package com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.mappers;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.Appointment;
import com.ruwalabs.saludya.appointments.domain.model.valueobjects.BookingOrder;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities.AppointmentJpaEntity;

/**
 * Maps between the {@link Appointment} domain aggregate and its JPA entity.
 */
public final class AppointmentPersistenceMapper {

    private AppointmentPersistenceMapper() {
    }

    public static AppointmentJpaEntity toJpaEntity(Appointment aggregate) {
        AppointmentJpaEntity entity = new AppointmentJpaEntity();
        entity.setId(aggregate.getId());
        entity.setTimeSlotId(aggregate.getTimeSlotId());
        entity.setPatientId(aggregate.getPatientId());
        entity.setBookingOrder(aggregate.getBookingOrder().value());
        entity.setBookingCode(aggregate.getBookingCode());
        entity.setStatus(aggregate.getStatus());
        entity.setCreatedAt(aggregate.getCreatedAt());
        entity.setUpdatedAt(aggregate.getUpdatedAt());
        return entity;
    }

    public static Appointment toDomain(AppointmentJpaEntity entity) {
        return Appointment.rehydrate(
                entity.getId(),
                entity.getTimeSlotId(),
                entity.getPatientId(),
                new BookingOrder(entity.getBookingOrder()),
                entity.getBookingCode(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
