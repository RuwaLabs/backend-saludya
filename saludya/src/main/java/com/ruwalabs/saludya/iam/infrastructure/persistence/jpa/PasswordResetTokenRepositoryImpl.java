package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa;

import com.ruwalabs.saludya.iam.domain.model.entities.PasswordResetToken;
import com.ruwalabs.saludya.iam.domain.repositories.PasswordResetTokenRepository;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.PasswordResetTokenEntity;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories.PasswordResetTokenJpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Optional;
@Repository
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {
    private final PasswordResetTokenJpaRepository tokens;
    public PasswordResetTokenRepositoryImpl(PasswordResetTokenJpaRepository tokens) { this.tokens=tokens; }
    public void save(PasswordResetToken t) {
        var e=new PasswordResetTokenEntity();e.setId(t.id());e.setUserId(t.userId());e.setTokenHash(t.tokenHash());
        e.setExpiresAt(t.expiresAt());e.setUsedAt(t.usedAt());tokens.saveAndFlush(e);
    }
    public Optional<Long> findOwnerByHash(String hash) {
        return tokens.findOwnerByHash(hash);
    }
    public Optional<PasswordResetToken> lockByHash(String hash) {
        return tokens.findByTokenHash(hash).map(e->new PasswordResetToken(e.getId(),e.getUserId(),e.getTokenHash(),e.getExpiresAt(),e.getUsedAt()));
    }
    public void invalidateByUserId(Long id,Instant now) { tokens.invalidateByUserId(id,now); }
}
