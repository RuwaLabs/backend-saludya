package com.ruwalabs.saludya.iam.application.results;

import java.time.Instant;

public record PasswordResetGrantResult(String resetToken, Instant expiresAt) {
    @Override public String toString() { return "PasswordResetGrantResult[REDACTED]"; }
}
