package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
@Getter @Setter @NoArgsConstructor
@Entity @Table(name="password_reset_tokens")
public class PasswordResetTokenEntity {
    @Id @JdbcTypeCode(SqlTypes.VARCHAR) @Column(length=36) private UUID id=UUID.randomUUID();
    @Column(name="user_id",nullable=false) private Long userId;
    @Column(name="token_hash",nullable=false,unique=true,length=64) private String tokenHash;
    @Column(name="expires_at",nullable=false) private java.time.Instant expiresAt;
    @Column(name="used_at") private java.time.Instant usedAt;
}
