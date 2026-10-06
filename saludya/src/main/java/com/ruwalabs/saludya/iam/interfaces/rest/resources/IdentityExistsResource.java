package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import jakarta.validation.constraints.*;
public record IdentityExistsResource(@NotBlank @Pattern(regexp="[0-9]{8}") String dni) { }
