package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.NotificationOutboxEntity;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;
public interface NotificationOutboxJpaRepository extends JpaRepository<NotificationOutboxEntity,UUID> {
    List<NotificationOutboxEntity> findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAt(String status,Instant now);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from NotificationOutboxEntity n where n.id=:id")
    Optional<NotificationOutboxEntity> lockById(UUID id);
}
