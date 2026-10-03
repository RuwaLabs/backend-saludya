package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @Entity @Table(name="identity_claims")
public class IdentityClaimEntity {
    @Id @Column(length=8) private String dni;
    @Column(name="user_id",nullable=false,unique=true) private Long userId;
}
