package com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities;

import com.ruwalabs.saludya.appointments.domain.model.valueobjects.TimeSlotStatus;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.converters.TimeSlotStatusAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Persistence entity mapped to the {@code time_slots} table.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "time_slot")
public class TimeSlotJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_hour", nullable = false)
    private LocalTime startHour;

    @Column(name = "end_hour", nullable = false)
    private LocalTime endHour;

    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;

    @Column(name = "current_bookings", nullable = false)
    private int currentBookings;

    @Column(name = "status", nullable = false)
    @Convert(converter = TimeSlotStatusAttributeConverter.class)
    private TimeSlotStatus status;
}
