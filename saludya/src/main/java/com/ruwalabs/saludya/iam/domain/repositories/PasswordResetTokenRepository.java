package com.ruwalabs.saludya.iam.domain.repositories;

import com.ruwalabs.saludya.iam.domain.model.entities.PasswordResetToken;
import java.time.Instant;
import java.util.Optional;
public interface PasswordResetTokenRepository {
    void save(PasswordResetToken token);
    Optional<Long> findOwnerByHash(String hash);
    Optional<PasswordResetToken> lockByHash(String hash);
    void invalidateByUserId(Long id, Instant now);
}
