package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import jakarta.validation.constraints.*;
public record ChangePasswordResource(@NotBlank @Size(max=72) String currentPassword,
        @NotBlank @Size(min=8,max=72) String password, @NotBlank @Size(min=8,max=72) String confirmPassword) {
    @Override public String toString() { return "ChangePasswordResource[REDACTED]"; }
}
