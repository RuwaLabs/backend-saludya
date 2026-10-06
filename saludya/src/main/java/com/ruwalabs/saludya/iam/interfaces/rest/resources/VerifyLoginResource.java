package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import jakarta.validation.constraints.*;
public record VerifyLoginResource(@NotBlank String challengeId,
        @NotBlank @Pattern(regexp="[0-9]{6}") String code) {
    @Override public String toString() { return "VerifyLoginResource[REDACTED]"; }
}
