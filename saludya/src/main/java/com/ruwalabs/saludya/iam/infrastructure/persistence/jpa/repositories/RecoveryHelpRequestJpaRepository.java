package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.RecoveryHelpRequestEntity;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.util.UUID;
public interface RecoveryHelpRequestJpaRepository extends JpaRepository<RecoveryHelpRequestEntity,UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RecoveryHelpRequestEntity r where r.id=:id")
    Optional<RecoveryHelpRequestEntity> lockById(UUID id);
    List<RecoveryHelpRequestEntity> findTop100ByStatusOrderByCreatedAtAsc(String status);
}
