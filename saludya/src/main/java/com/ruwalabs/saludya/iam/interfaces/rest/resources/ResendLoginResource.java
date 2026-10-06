package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import jakarta.validation.constraints.*;
public record ResendLoginResource(@NotBlank String challengeId) {
    @Override public String toString() { return "ResendLoginResource[REDACTED]"; }
}
