package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
@Getter @Setter @NoArgsConstructor
@Entity @Table(name="iam_notification_outboxes")
public class NotificationOutboxEntity {
    @Id @JdbcTypeCode(SqlTypes.VARCHAR) @Column(length=36) private UUID id=UUID.randomUUID();
    @Column(nullable=false,length=150) private String recipient;
    @Column(nullable=false,length=150) private String subject;
    @Column(name="encrypted_body",nullable=false,columnDefinition="text") private String encryptedBody;
    @Column(nullable=false,length=20) private String status="PENDING";
    @Column(nullable=false) private int attempts;
    @Column(name="next_attempt_at",nullable=false) private java.time.Instant nextAttemptAt;
    @Column(name="created_at",nullable=false) private java.time.Instant createdAt;
    @Column(name="sent_at") private java.time.Instant sentAt;
}
