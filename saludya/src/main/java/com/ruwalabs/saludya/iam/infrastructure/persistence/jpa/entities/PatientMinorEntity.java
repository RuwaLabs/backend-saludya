package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.*;
import com.ruwalabs.saludya.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
@Getter @Setter @NoArgsConstructor
@Entity @Table(name="patient_minors")
public class PatientMinorEntity extends AuditableAbstractPersistenceEntity {
    @Column(name="id_patient",nullable=false,unique=true) private Long patientId;
    @Column(name="id_tutor",nullable=false) private Long tutorId;
}
