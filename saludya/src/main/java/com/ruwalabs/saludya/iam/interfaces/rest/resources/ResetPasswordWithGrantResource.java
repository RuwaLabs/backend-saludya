package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordWithGrantResource(
        @NotBlank String resetToken,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(min = 8, max = 72) String confirmPassword) {
    @Override public String toString() { return "ResetPasswordWithGrantResource[REDACTED]"; }
}
