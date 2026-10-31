package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyPasswordRecoveryCodeResource(
        @NotBlank String challengeId,
        @NotBlank @Pattern(regexp = "[0-9]{6}") String code) {
    @Override public String toString() { return "VerifyPasswordRecoveryCodeResource[REDACTED]"; }
}
