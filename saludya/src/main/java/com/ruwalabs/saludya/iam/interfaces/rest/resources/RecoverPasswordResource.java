package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import jakarta.validation.constraints.*;
public record RecoverPasswordResource(@NotBlank @Email @Size(max=150) String email) { }
