package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import jakarta.validation.constraints.*;
public record SendVerificationCodeResource(@NotBlank @Email @Size(max=150) String email) {
    @Override public String toString() { return "SendVerificationCodeResource[REDACTED]"; }
}
