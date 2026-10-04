package com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.entities;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.QueueEntryStatus;
import com.ruwalabs.saludya.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA persistence entity for attendance queue entries (table {@code queue_entries}).
 */
@Entity
@Table(name = "queue_entries")
@Getter
@Setter
@NoArgsConstructor
public class QueueEntryPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "id_attendance_queue", nullable = false)
    private Long idAttendanceQueue;

    @Column(name = "id_check_in", nullable = false, unique = true)
    private Long idCheckIn;

    @Column(name = "position", nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QueueEntryStatus status;

    @Column(name = "called_at")
    private Instant calledAt;

    @Column(name = "attended_at")
    private Instant attendedAt;
}
