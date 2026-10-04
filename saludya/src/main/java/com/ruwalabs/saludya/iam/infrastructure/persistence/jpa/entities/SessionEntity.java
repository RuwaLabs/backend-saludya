package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
@Getter @Setter @NoArgsConstructor
@Entity @Table(name="user_sessions")
public class SessionEntity {
    @Id @JdbcTypeCode(SqlTypes.VARCHAR) @Column(length=36) private UUID id=UUID.randomUUID();
    @Column(name="user_id",nullable=false) private Long userId;
    @Column(name="expires_at",nullable=false) private java.time.Instant expiresAt;
    @Column(name="revoked_at") private java.time.Instant revokedAt;
}
