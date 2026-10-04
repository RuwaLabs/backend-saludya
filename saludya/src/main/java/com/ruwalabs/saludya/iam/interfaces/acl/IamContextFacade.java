package com.ruwalabs.saludya.iam.interfaces.acl;

import com.ruwalabs.saludya.iam.application.internal.AccessPolicy;
import com.ruwalabs.saludya.iam.application.internal.queryservices.UserAccountQueryServiceImpl;
import com.ruwalabs.saludya.iam.application.results.AccountProfile;
import com.ruwalabs.saludya.iam.domain.repositories.*;
import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
/** Internal contract. REST controllers must authorize the caller before returning these views. */
@Service @Transactional(readOnly=true)
public class IamContextFacade {
    public record UserView(Long id,String email,Role role,boolean active) { }
    public record PatientView(Long id,Long userId,String dni,String name,String lastname,LocalDate birthDate,String phone) { }
    private final UserAccountRepository users;private final PatientRepository patients;private final PatientMinorRepository minors;
    private final AccessPolicy access;
    public IamContextFacade(UserAccountRepository users,PatientRepository patients,PatientMinorRepository minors,AccessPolicy access) {
        this.users=users;this.patients=patients;this.minors=minors;this.access=access;
    }
    public Optional<UserView> getUserById(Long id) { return users.findById(id).map(u->new UserView(u.getId(),u.getEmail().value(),u.getRole(),u.isActive())); }
    public Optional<PatientView> getPatientById(Long id) {
        return patients.findById(id).map(p->new PatientView(p.getId(),p.getUserId(),p.getDni().value(),p.getName(),p.getLastname(),p.getBirthDate(),p.getPhone()));
    }
    public List<PatientView> getMinorsByTutor(Long tutorPatientId) {
        return minors.findMinorsByTutor(tutorPatientId).stream().map(l->getPatientById(l.patientId())).flatMap(Optional::stream).toList();
    }
    public boolean canManagePatient(Long accountId,Long patientId) {
        return users.findById(accountId).filter(u->u.isActive()&&u.getRole()==Role.PATIENT).isPresent()
                &&patients.findById(patientId).map(p->access.managesPatient(accountId,p)).orElse(false);
    }
    public Optional<String> getContactEmailForPatient(Long patientId) {
        return patients.findById(patientId).flatMap(p->p.getUserId()!=null?getUserById(p.getUserId()):
                minors.findByPatientId(patientId).flatMap(l->patients.findById(l.tutorId())).flatMap(t->getUserById(t.getUserId())))
                .filter(UserView::active).map(UserView::email);
    }
}
