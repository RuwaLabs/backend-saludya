package com.ruwalabs.saludya.iam.domain.services;

import com.ruwalabs.saludya.iam.domain.model.aggregates.UserAccount;
import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import java.time.Instant;
import java.util.UUID;
public interface TokenService {
    String issue(UserAccount account, UUID sessionId, Instant expiresAt);
    TokenClaims verify(String token);
    record TokenClaims(Long userId, Role role, UUID sessionId, Instant expiresAt) {}
}
