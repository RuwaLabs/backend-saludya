package com.ruwalabs.saludya.iam.application.internal.commandservices;

import com.ruwalabs.saludya.iam.application.commands.*;
import com.ruwalabs.saludya.iam.application.commandservices.PatientCommandService;
import com.ruwalabs.saludya.iam.application.internal.*;
import com.ruwalabs.saludya.iam.domain.repositories.*;
import com.ruwalabs.saludya.iam.domain.services.EventPublisher;
import com.ruwalabs.saludya.iam.domain.model.aggregates.Patient;
import com.ruwalabs.saludya.iam.domain.model.entities.PatientMinor;
import com.ruwalabs.saludya.iam.domain.model.events.*;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional
public class PatientCommandServiceImpl implements PatientCommandService {
    private final PatientRepository patients;private final PatientMinorRepository links;
    private final IdentityVerificationService identities;private final AccessPolicy access;private final EventPublisher events;
    public PatientCommandServiceImpl(PatientRepository patients,PatientMinorRepository links,IdentityVerificationService identities,
            AccessPolicy access,EventPublisher events) {
        this.patients=patients;this.links=links;this.identities=identities;this.access=access;this.events=events;
    }
    public PatientMinor linkMinor(LinkMinorCommand c) {
        var tutor=access.requireAdultPatient(c.actorId());
        if (tutor.isMinor(identities.today())) throw new IamException(422,"IAM_ADULT_REQUIRED","The tutor must be an adult");
        var identity=identities.verify(c.dni(),c.name(),c.lastname(),c.birthDate());
        if (!identity.birthDate().plusYears(18).isAfter(identities.today()))
            throw new IamException(422,"IAM_MINOR_REQUIRED","The beneficiary must be younger than 18");
        var existing=patients.findByDni(c.dni());
        if (existing.isPresent() && links.findByPatientId(existing.get().getId()).isPresent())
            throw new IamException(409,"IAM_MINOR_ALREADY_LINKED","The minor is already linked. Contact admission to validate legal guardianship");
        if (!c.confirmFiliation() || identity.guardianDnis()==null || !identity.guardianDnis().contains(tutor.getDni().value()))
            throw new IamException(422,"IAM_GUARDIANSHIP_UNVERIFIED","The legal relationship could not be verified. Contact admission");
        var minor=existing.orElseGet(()->patients.save(new Patient(null,null,identity.dni(),identity.name(),
                identity.lastname(),identity.birthDate(),null)));
        if (minor.getUserId()!=null) throw new IamException(409,"IAM_BENEFICIARY_HAS_ACCOUNT","This identity already has an independent account");
        patients.lockById(minor.getId()).orElseThrow(IamException::notFound);
        if (links.findByPatientId(minor.getId()).isPresent())
            throw new IamException(409,"IAM_MINOR_ALREADY_LINKED","The minor is already linked. Contact admission");
        var link=links.save(new PatientMinor(null,minor.getId(),tutor.getId()));
        events.publish(new MinorLinkedEvent(c.actorId(),minor.getId()));return link;
    }
    public void unlinkMinor(UnlinkMinorCommand c) {
        var tutor=access.requireAdultPatient(c.actorId());
        var link=links.findById(c.linkId()).orElseThrow(IamException::notFound);
        if (!link.tutorId().equals(tutor.getId())) throw IamException.forbidden();
        patients.lockById(link.patientId()).orElseThrow(IamException::notFound);
        links.delete(link);events.publish(new MinorUnlinkedEvent(c.actorId(),link.patientId()));
    }
}
