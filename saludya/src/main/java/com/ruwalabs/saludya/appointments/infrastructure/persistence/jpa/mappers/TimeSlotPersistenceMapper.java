package com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.mappers;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.TimeSlot;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities.TimeSlotJpaEntity;

/**
 * Maps between the {@link TimeSlot} domain aggregate and its JPA entity.
 */
public final class TimeSlotPersistenceMapper {

    private TimeSlotPersistenceMapper() {
    }

    public static TimeSlotJpaEntity toJpaEntity(TimeSlot aggregate) {
        TimeSlotJpaEntity entity = new TimeSlotJpaEntity();
        entity.setId(aggregate.getId());
        entity.setDoctorId(aggregate.getDoctorId());
        entity.setDate(aggregate.getDate());
        entity.setStartHour(aggregate.getStartHour());
        entity.setEndHour(aggregate.getEndHour());
        entity.setRoom(aggregate.getRoom());
        entity.setMaxCapacity(aggregate.getMaxCapacity());
        entity.setCurrentBookings(aggregate.getCurrentBookings());
        entity.setStatus(aggregate.getStatus());
        return entity;
    }

    public static TimeSlot toDomain(TimeSlotJpaEntity entity) {
        return TimeSlot.rehydrate(
                entity.getId(),
                entity.getDoctorId(),
                entity.getDate(),
                entity.getStartHour(),
                entity.getEndHour(),
                entity.getRoom(),
                entity.getMaxCapacity(),
                entity.getCurrentBookings(),
                entity.getStatus()
        );
    }
}
