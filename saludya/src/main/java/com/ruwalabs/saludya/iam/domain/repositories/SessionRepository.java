package com.ruwalabs.saludya.iam.domain.repositories;

import com.ruwalabs.saludya.iam.domain.model.entities.UserSession;
import java.time.Instant;
import java.util.*;
public interface SessionRepository {
    void save(UserSession session);
    Optional<UserSession> findById(UUID id);
    void revoke(UUID id, Instant now);
    void revokeByUserId(Long id, Instant now);
}
