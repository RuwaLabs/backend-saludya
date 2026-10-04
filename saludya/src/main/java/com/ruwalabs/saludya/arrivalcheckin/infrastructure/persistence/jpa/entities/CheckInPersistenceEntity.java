package com.ruwalabs.saludya.arrivalcheckin.infrastructure.persistence.jpa.entities;

import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.CheckInStatus;
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
 * JPA persistence entity for check-ins (table {@code check_ins}).
 */
@Entity
@Table(name = "check_ins")
@Getter
@Setter
@NoArgsConstructor
public class CheckInPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "id_appointment", nullable = false, unique = true)
    private Long idAppointment;

    @Column(name = "qr_token", length = 500)
    private String qrToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CheckInStatus status;

    @Column(name = "checked_in_at", nullable = false)
    private Instant checkedInAt;
}
