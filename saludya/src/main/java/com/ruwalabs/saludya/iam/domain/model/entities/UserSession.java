package com.ruwalabs.saludya.iam.domain.model.entities;

import java.time.Instant;
import java.util.UUID;
public record UserSession(UUID id, Long userId, Instant expiresAt, Instant revokedAt) {
    public boolean isValid(Instant now) { return revokedAt == null && expiresAt.isAfter(now); }
}
