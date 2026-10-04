package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record CreateStaffAccountResource(@NotBlank @Pattern(regexp="[0-9]{8}") String dni,
        @NotBlank @Size(max=100) String name, @NotBlank @Size(max=100) String lastname,
        @NotNull @Past LocalDate birthDate, @NotBlank @Pattern(regexp="\\+?[0-9]{9,15}") String phone,
        @NotBlank @Email @Size(max=150) String email) { }
