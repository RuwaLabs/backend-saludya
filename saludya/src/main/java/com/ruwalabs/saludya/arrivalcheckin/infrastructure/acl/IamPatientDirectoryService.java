package com.ruwalabs.saludya.arrivalcheckin.infrastructure.acl;

import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.PatientDirectoryService;
import com.ruwalabs.saludya.iam.interfaces.acl.IamContextFacade;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("arrivalPatientDirectoryService")
public class IamPatientDirectoryService implements PatientDirectoryService {

    private final IamContextFacade iamContextFacade;

    public IamPatientDirectoryService(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    public Optional<PatientSummary> findPatient(Long patientId) {
        return iamContextFacade.getPatientById(patientId)
                .map(patient -> new PatientSummary(patient.id(), patient.name(), patient.lastname()));
    }
}
