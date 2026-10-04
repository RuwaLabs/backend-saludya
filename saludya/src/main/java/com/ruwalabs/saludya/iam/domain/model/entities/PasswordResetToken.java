package com.ruwalabs.saludya.iam.domain.model.entities;

import java.time.Instant;
import java.util.UUID;
public record PasswordResetToken(UUID id, Long userId, String tokenHash, Instant expiresAt, Instant usedAt) {
    public boolean usable(Instant now) { return usedAt == null && expiresAt.isAfter(now); }
}
