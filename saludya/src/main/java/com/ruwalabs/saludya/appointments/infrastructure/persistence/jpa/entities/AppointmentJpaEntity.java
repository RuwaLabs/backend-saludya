package com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities;

import com.ruwalabs.saludya.appointments.domain.model.valueobjects.AppointmentStatus;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.converters.AppointmentStatusAttributeConverter;
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

import java.time.Instant;

/**
 * Persistence entity mapped to the {@code appointments} table.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "appointment")
public class AppointmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time_slot_id", nullable = false)
    private Long timeSlotId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "booking_order", nullable = false)
    private int bookingOrder;

    @Column(name = "status", nullable = false)
    @Convert(converter = AppointmentStatusAttributeConverter.class)
    private AppointmentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
