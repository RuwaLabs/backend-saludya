package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import jakarta.validation.constraints.*;
public record ResetPasswordResource(@NotBlank @Size(max=128) String token,
        @NotBlank @Size(min=8,max=72) String password, @NotBlank @Size(min=8,max=72) String confirmPassword) {
    @Override public String toString() { return "ResetPasswordResource[REDACTED]"; }
}
