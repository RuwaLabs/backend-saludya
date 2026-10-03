package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.*;
import com.ruwalabs.saludya.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
@Getter @Setter @NoArgsConstructor
@Entity @Table(name="users")
public class UserAccountEntity extends AuditableAbstractPersistenceEntity {
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="id_role",nullable=false) private RoleEntity role;
    @Column(nullable=false,unique=true,length=150) private String email;
    @Column(nullable=false,length=255) private String password;
    @Column(name="is_active",nullable=false) private boolean active;
}
