package com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl;

import java.util.Optional;

/** Read-only patient identity data needed by administrative queue views. */
public interface PatientDirectoryService {

    record PatientSummary(Long id, String name, String lastname) {
        public String fullName() {
            return (name + " " + lastname).trim();
        }
    }

    Optional<PatientSummary> findPatient(Long patientId);
}
