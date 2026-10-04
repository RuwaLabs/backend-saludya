package com.ruwalabs.saludya.iam.infrastructure.persistence.jpa;

import com.ruwalabs.saludya.iam.domain.model.entities.UserSession;
import com.ruwalabs.saludya.iam.domain.repositories.SessionRepository;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.SessionEntity;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories.SessionJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
@Repository @Transactional(readOnly=true)
public class SessionRepositoryImpl implements SessionRepository {
    private final SessionJpaRepository sessions;
    public SessionRepositoryImpl(SessionJpaRepository sessions) { this.sessions=sessions; }
    @Override @Transactional public void save(UserSession s) {
        var e=new SessionEntity();e.setId(s.id());e.setUserId(s.userId());e.setExpiresAt(s.expiresAt());e.setRevokedAt(s.revokedAt());sessions.saveAndFlush(e);
    }
    @Override public Optional<UserSession> findById(UUID id) {
        return sessions.findById(id).map(e->new UserSession(e.getId(),e.getUserId(),e.getExpiresAt(),e.getRevokedAt()));
    }
    @Override @Transactional public void revoke(UUID id,Instant now) { sessions.revoke(id,now); }
    @Override @Transactional public void revokeByUserId(Long id,Instant now) { sessions.revokeByUserId(id,now); }
}
