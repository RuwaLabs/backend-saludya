package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.*;
import com.ruwalabs.saludya.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
@Getter @Setter @NoArgsConstructor
@Entity @Table(name="staff_profiles")
public class StaffProfileEntity extends AuditableAbstractPersistenceEntity {
    @Column(name="id_user",nullable=false,unique=true) private Long userId;
    @Column(nullable=false,unique=true,length=8) private String dni;
    @Column(nullable=false,length=100) private String name;
    @Column(nullable=false,length=100) private String lastname;
    @Column(name="birth_date",nullable=false) private java.time.LocalDate birthDate;
    @Column(nullable=false,length=20) private String phone;
}
