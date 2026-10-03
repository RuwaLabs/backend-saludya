package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import jakarta.validation.constraints.*;
import com.ruwalabs.saludya.iam.domain.model.enums.Role;
public record LoginResource(@Email @Size(max=150) String email, @Pattern(regexp="[0-9]{8}") String dni,
        @NotBlank @Size(max=72) String password, @NotNull Role role) {
    @Override public String toString() { return "LoginResource[REDACTED]"; }
}
