package com.ruwalabs.saludya.iam.application.internal;

import com.ruwalabs.saludya.iam.domain.model.entities.*;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.*;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import com.ruwalabs.saludya.iam.domain.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.util.*;
/** Assisted recovery is a separate privileged operation after an in-person identity check. */
@Service @Transactional
public class AccountRecoveryHelpService {
    private final RecoveryHelpRequestRepository requests;private final AccessPolicy access;
    private final PatientRepository patients;private final StaffProfileRepository staff;private final UserAccountRepository users;
    private final PasswordRecoveryService recovery;private final SessionRepository sessions;private final Clock clock;
    private final com.ruwalabs.saludya.iam.domain.services.NotificationGateway notifications;
    public AccountRecoveryHelpService(RecoveryHelpRequestRepository requests,AccessPolicy access,PatientRepository patients,
            StaffProfileRepository staff,UserAccountRepository users,PasswordRecoveryService recovery,SessionRepository sessions,Clock clock,
            com.ruwalabs.saludya.iam.domain.services.NotificationGateway notifications) {
        this.notifications=notifications;
        this.requests=requests;this.access=access;this.patients=patients;this.staff=staff;this.users=users;
        this.recovery=recovery;this.sessions=sessions;this.clock=clock;
    }
    public void request(String dni,String email) {
        requests.save(new RecoveryHelpRequest(UUID.randomUUID(),new Dni(dni).value(),new Email(email).value(),clock.instant(),"OPEN",null,null));
    }
    @Transactional(readOnly=true) public List<RecoveryHelpRequest> open(Long actorId) {
        access.requireSuperAdmin(actorId);return requests.findOpen();
    }
    public void resolve(Long actorId,UUID id,boolean identityCheckedInPerson) {
        access.requireSuperAdmin(actorId);
        if(!identityCheckedInPerson) throw new IamException(422,"IAM_IN_PERSON_CHECK_REQUIRED","Check the physical identity document before assisted recovery");
        var request=requests.lockById(id).orElseThrow(IamException::notFound);
        if(!request.status().equals("OPEN")) throw new IamException(409,"IAM_RECOVERY_ALREADY_RESOLVED","The request is already resolved");
        Long userId=patients.findByDni(request.dni()).map(p->p.getUserId()).orElseGet(
                ()->staff.findByDni(request.dni()).map(StaffProfile::userId).orElse(null));
        if(userId==null) throw IamException.notFound();
        var user=users.lockById(userId).orElseThrow(IamException::notFound);
        if(!user.isActive()) throw IamException.forbidden();
        var email=new Email(request.contactEmail());
        if(users.findByEmail(email.value()).filter(u->!u.getId().equals(userId)).isPresent())
            throw new IamException(409,"IAM_EMAIL_ALREADY_REGISTERED","The email is already registered");
        String old=user.getEmail().value();user.updateEmail(email);users.save(user);
        sessions.revokeByUserId(userId,clock.instant());recovery.request(email.value());
        if(!old.equals(email.value())) notifications.enqueue(old,"Recuperación asistida de SaludYa",
                "Un administrador verificó la identidad del titular y actualizó el correo de acceso. Se cerraron las sesiones anteriores. Si no solicitaste este cambio, contacta al área de admisión.");
        requests.save(new RecoveryHelpRequest(request.id(),request.dni(),request.contactEmail(),request.createdAt(),
                "RESOLVED",actorId,clock.instant()));
    }
}
