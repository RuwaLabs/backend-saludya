package com.ruwalabs.saludya.iam.application.internal;

import com.ruwalabs.saludya.iam.domain.repositories.*;
import com.ruwalabs.saludya.iam.domain.model.aggregates.*;
import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import org.springframework.stereotype.Service;
@Service
public class AccessPolicy {
    private final UserAccountRepository users;private final PatientRepository patients;private final PatientMinorRepository links;
    public AccessPolicy(UserAccountRepository users,PatientRepository patients,PatientMinorRepository links) { this.users=users;this.patients=patients;this.links=links; }
    public UserAccount actor(Long id) {
        var user=users.findById(id).orElseThrow(IamException::unauthorized);
        if (!user.isActive()) throw IamException.unauthorized(); return user;
    }
    public void requireSelf(Long actorId,Long userId) {
        actor(actorId);if (!actorId.equals(userId)) throw IamException.forbidden();
    }
    public void requireSuperAdmin(Long actorId) {
        if (actor(actorId).getRole()!=Role.SUPER_ADMIN) throw IamException.forbidden();
    }
    public Patient requireAdultPatient(Long actorId) {
        if (actor(actorId).getRole()!=Role.PATIENT) throw IamException.forbidden();
        return patients.findByUserId(actorId).orElseThrow(IamException::notFound);
    }
    public boolean managesPatient(Long actorId,Patient patient) {
        if (actorId.equals(patient.getUserId())) return true;
        return links.findByPatientId(patient.getId()).flatMap(l->patients.findById(l.tutorId()))
                .map(t->actorId.equals(t.getUserId())).orElse(false);
    }
    public void requirePatientRead(Long actorId,Patient patient) {
        var user=actor(actorId);
        if (user.getRole()!=Role.PATIENT || managesPatient(actorId,patient)) return;
        throw IamException.forbidden();
    }
}
