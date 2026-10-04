package com.ruwalabs.saludya.iam.interfaces.rest.transform;

import com.ruwalabs.saludya.iam.domain.model.aggregates.Patient;
import com.ruwalabs.saludya.iam.interfaces.rest.resources.PatientResource;
public final class PatientResourceAssembler {
    private PatientResourceAssembler() { }
    public static PatientResource from(Patient p) {
        return new PatientResource(p.getId(),p.getUserId(),p.getDni().value(),p.getName(),p.getLastname(),p.getBirthDate(),p.getPhone());
    }
}
