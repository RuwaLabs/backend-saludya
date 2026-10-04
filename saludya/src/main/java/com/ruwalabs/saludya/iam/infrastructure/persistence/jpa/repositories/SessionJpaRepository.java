package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.SessionEntity;
import org.springframework.data.jpa.repository.*;
import java.time.Instant;
import java.util.UUID;
public interface SessionJpaRepository extends JpaRepository<SessionEntity,UUID> {
    @Modifying @Query("update SessionEntity s set s.revokedAt=:now where s.id=:id and s.revokedAt is null")
    void revoke(UUID id, Instant now);
    @Modifying @Query("update SessionEntity s set s.revokedAt=:now where s.userId=:userId and s.revokedAt is null")
    void revokeByUserId(Long userId, Instant now);
}
