package com.ruwalabs.saludya.iam.domain.model.valueobjects;

public record PasswordHash(String value) {
    public PasswordHash {
        if (value == null || !value.matches("\\$2[aby]\\$[0-9]{2}\\$.{53}"))
            throw new IllegalArgumentException("Password must be stored as a BCrypt hash");
    }
    @Override public String toString() { return "[REDACTED]"; }
}
