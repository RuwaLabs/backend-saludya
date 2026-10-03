package com.ruwalabs.saludya.iam.domain.model.entities;

import java.util.Objects;
public record PatientMinor(Long id, Long patientId, Long tutorId) {
    public PatientMinor {
        Objects.requireNonNull(patientId); Objects.requireNonNull(tutorId);
        if (patientId.equals(tutorId)) throw new IllegalArgumentException("A patient cannot be their own tutor");
    }
}
