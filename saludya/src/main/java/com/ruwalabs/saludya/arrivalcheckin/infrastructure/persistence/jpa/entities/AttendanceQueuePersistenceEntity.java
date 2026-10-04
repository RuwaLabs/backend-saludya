package com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.entities;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.AttendanceQueueStatus;
import com.ruwalabs.saludya.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * JPA persistence entity for attendance queues (table {@code attendance_queues}).
 */
@Entity
@Table(name = "attendance_queues")
@Getter
@Setter
@NoArgsConstructor
public class AttendanceQueuePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "id_time_slot", nullable = false)
    private Long idTimeSlot;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttendanceQueueStatus status;
}
