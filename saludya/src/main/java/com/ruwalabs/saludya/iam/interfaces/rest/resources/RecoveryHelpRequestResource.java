package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import jakarta.validation.constraints.*;
public record RecoveryHelpRequestResource(@NotBlank @Pattern(regexp="[0-9]{8}") String dni,
        @NotBlank @Email @Size(max=150) String contactEmail) { }
