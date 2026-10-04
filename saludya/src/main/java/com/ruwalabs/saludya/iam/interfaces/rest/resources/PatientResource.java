package com.ruwalabs.saludya.iam.interfaces.rest.resources;

import java.time.LocalDate;
public record PatientResource(Long id, Long userId, String dni, String name, String lastname,
        LocalDate birthDate, String phone) { }
