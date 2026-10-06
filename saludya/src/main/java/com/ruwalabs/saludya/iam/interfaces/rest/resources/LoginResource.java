package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import jakarta.validation.constraints.*;
public record LoginResource(@NotBlank @Email @Size(max=150) String email,
        @NotBlank @Size(max=72) String password) {
    @Override public String toString() { return "LoginResource[REDACTED]"; }
}
