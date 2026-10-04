package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
@Getter @Setter @NoArgsConstructor
@Entity @Table(name="account_recovery_requests")
public class RecoveryHelpRequestEntity {
    @Id @JdbcTypeCode(SqlTypes.VARCHAR) @Column(length=36) private UUID id=UUID.randomUUID();
    @Column(nullable=false,length=8) private String dni;
    @Column(name="contact_email",nullable=false,length=150) private String contactEmail;
    @Column(name="created_at",nullable=false) private java.time.Instant createdAt;
    @Column(nullable=false,length=20) private String status="OPEN";
    @Column(name="resolved_by") private Long resolvedBy;
    @Column(name="resolved_at") private java.time.Instant resolvedAt;
}
