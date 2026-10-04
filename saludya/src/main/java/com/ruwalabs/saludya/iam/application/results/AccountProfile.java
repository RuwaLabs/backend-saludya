package com.ruwalabs.saludya.iam.application.results;

import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import java.time.LocalDate;
public record AccountProfile(Long id, Role role, String email, boolean active, Long patientId,
                             String dni, String name, String lastname, LocalDate birthDate, String phone) {}
