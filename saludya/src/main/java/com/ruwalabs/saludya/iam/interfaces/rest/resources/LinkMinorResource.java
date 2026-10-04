package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record LinkMinorResource(@NotBlank @Pattern(regexp="[0-9]{8}") String dni,
        @NotBlank @Size(max=100) String name, @NotBlank @Size(max=100) String lastname,
        @NotNull @Past LocalDate birthDate, boolean confirmFiliation) { }
