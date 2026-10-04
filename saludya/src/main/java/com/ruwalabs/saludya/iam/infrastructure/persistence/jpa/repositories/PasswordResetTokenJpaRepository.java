package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;
public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenEntity,UUID> {
    @Query("select t.userId from PasswordResetTokenEntity t where t.tokenHash=:hash")
    Optional<Long> findOwnerByHash(String hash);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);
    @Modifying(clearAutomatically=true,flushAutomatically=true)
    @Query("update PasswordResetTokenEntity t set t.usedAt=:now where t.userId=:userId and t.usedAt is null")
    void invalidateByUserId(Long userId, Instant now);
}
