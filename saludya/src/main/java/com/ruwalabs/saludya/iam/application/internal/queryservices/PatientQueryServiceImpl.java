package com.ruwalabs.saludya.iam.application.internal.queryservices;

import com.ruwalabs.saludya.iam.application.queryservices.PatientQueryService;
import com.ruwalabs.saludya.iam.application.internal.AccessPolicy;
import com.ruwalabs.saludya.iam.domain.repositories.*;
import com.ruwalabs.saludya.iam.domain.model.aggregates.Patient;
import com.ruwalabs.saludya.iam.domain.model.entities.PatientMinor;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service @Transactional(readOnly=true)
public class PatientQueryServiceImpl implements PatientQueryService {
    private final PatientRepository patients;private final PatientMinorRepository links;private final AccessPolicy access;
    public PatientQueryServiceImpl(PatientRepository patients,PatientMinorRepository links,AccessPolicy access) {
        this.patients=patients;this.links=links;this.access=access;
    }
    public Patient getById(Long actorId,Long id) {
        var p=patients.findById(id).orElseThrow(IamException::notFound);access.requirePatientRead(actorId,p);return p;
    }
    public List<PatientMinor> getMinorsByTutor(Long actorId,Long tutorId) {
        var tutor=access.requireAdultPatient(actorId);if (!tutor.getId().equals(tutorId)) throw IamException.forbidden();
        return links.findMinorsByTutor(tutorId);
    }
    public PatientMinor getLinkById(Long actorId,Long id) {
        var tutor=access.requireAdultPatient(actorId);
        var l=links.findById(id).orElseThrow(IamException::notFound);
        if (!l.tutorId().equals(tutor.getId())) throw IamException.forbidden();return l;
    }
}
