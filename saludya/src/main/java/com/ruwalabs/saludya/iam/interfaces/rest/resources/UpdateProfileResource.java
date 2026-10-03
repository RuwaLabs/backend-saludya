package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import jakarta.validation.constraints.*;
public record UpdateProfileResource(@NotBlank @Email @Size(max=150) String email,
        @NotBlank @Pattern(regexp="\\+?[0-9]{9,15}") String phone) { }
