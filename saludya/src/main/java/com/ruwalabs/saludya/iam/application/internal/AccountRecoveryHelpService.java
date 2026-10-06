package com.ruwalabs.saludya.iam.application.internal;

import com.ruwalabs.saludya.iam.application.results.RecoveryResolution;
import com.ruwalabs.saludya.iam.domain.model.entities.*;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.*;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import com.ruwalabs.saludya.iam.domain.repositories.*;
import com.ruwalabs.saludya.iam.domain.services.HashingService;
import com.ruwalabs.saludya.iam.domain.services.NotificationGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.*;
/** Assisted recovery is a separate privileged operation after an in-person identity check. */
@Service @Transactional
public class AccountRecoveryHelpService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final RecoveryHelpRequestRepository requests;private final AccessPolicy access;
    private final PatientRepository patients;private final StaffProfileRepository staff;private final UserAccountRepository users;
    private final PasswordRecoveryService recovery;private final SessionRepository sessions;private final Clock clock;
    private final NotificationGateway notifications;private final HashingService hashing;
    public AccountRecoveryHelpService(RecoveryHelpRequestRepository requests,AccessPolicy access,PatientRepository patients,
            StaffProfileRepository staff,UserAccountRepository users,PasswordRecoveryService recovery,SessionRepository sessions,Clock clock,
            NotificationGateway notifications,HashingService hashing) {
        this.notifications=notifications;this.hashing=hashing;
        this.requests=requests;this.access=access;this.patients=patients;this.staff=staff;this.users=users;
        this.recovery=recovery;this.sessions=sessions;this.clock=clock;
    }
    public void request(String dni,String email) {
        requests.save(new RecoveryHelpRequest(UUID.randomUUID(),new Dni(dni).value(),new Email(email).value(),clock.instant(),"OPEN",null,null));
    }
    @Transactional(readOnly=true) public List<RecoveryHelpRequest> open(Long actorId) {
        access.requireSuperAdmin(actorId);return requests.findOpen();
    }
    /**
     * Restores an account after an in-person identity check: sets a new random email and a
     * new random temporary password, revokes every session, keeps the recovery link flowing
     * to the new email and returns the generated credentials so the operator can hand them
     * over to the account holder.
     */
    public RecoveryResolution resolve(Long actorId,UUID id,boolean identityCheckedInPerson) {
        access.requireSuperAdmin(actorId);
        if(!identityCheckedInPerson) throw new IamException(422,"IAM_IN_PERSON_CHECK_REQUIRED","Check the physical identity document before assisted recovery");
        var request=requests.lockById(id).orElseThrow(IamException::notFound);
        if(!request.status().equals("OPEN")) throw new IamException(409,"IAM_RECOVERY_ALREADY_RESOLVED","The request is already resolved");
        Long userId=patients.findByDni(request.dni()).map(p->p.getUserId()).orElseGet(
                ()->staff.findByDni(request.dni()).map(StaffProfile::userId).orElse(null));
        if(userId==null) throw IamException.notFound();
        var user=users.lockById(userId).orElseThrow(IamException::notFound);
        if(!user.isActive()) throw IamException.forbidden();

        var email=new Email(randomEmail(request.contactEmail()));
        if(users.findByEmail(email.value()).filter(u->!u.getId().equals(userId)).isPresent())
            throw new IamException(409,"IAM_EMAIL_ALREADY_REGISTERED","The email is already registered");
        var newPassword=randomPassword();

        String old=user.getEmail().value();
        user.updateEmail(email);user.changePassword(hashing.hash(newPassword));users.save(user);
        sessions.revokeByUserId(userId,clock.instant());
        recovery.request(email.value());
        if(!old.equals(email.value())) notifications.enqueue(old,"Recuperación asistida de SaludYa",
                "Un administrador verificó la identidad del titular y restableció las credenciales de acceso. Se cerraron las sesiones anteriores. Si no solicitaste este cambio, contacta al área de admisión.");
        requests.save(new RecoveryHelpRequest(request.id(),request.dni(),request.contactEmail(),request.createdAt(),
                "RESOLVED",actorId,clock.instant()));
        return new RecoveryResolution(email.value(),newPassword,
                "Credentials reset. Share the new email and temporary password with the account holder.");
    }

    /** Builds a fresh, unique address on the contact email's domain (an alias when possible). */
    private static String randomEmail(String contactEmail) {
        var token=randomHex(8);
        int at=contactEmail==null?-1:contactEmail.indexOf('@');
        if(at<=0) return "recuperado."+token+"@saludya.local";
        var local=contactEmail.substring(0,at);
        int plus=local.indexOf('+');
        if(plus>=0) local=local.substring(0,plus);
        return local+"+rec"+token+"@"+contactEmail.substring(at+1);
    }
    private static String randomPassword() {
        var alphabet="ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        var chars=new char[12];
        for(int i=0;i<chars.length;i++) chars[i]=alphabet.charAt(RANDOM.nextInt(alphabet.length()));
        chars[0]="ABCDEFGHJKLMNPQRSTUVWXYZ".charAt(RANDOM.nextInt(24));
        chars[1]="23456789".charAt(RANDOM.nextInt(8));
        return new String(chars);
    }
    private static String randomHex(int length) {
        var hex="0123456789abcdef";var sb=new StringBuilder(length);
        for(int i=0;i<length;i++) sb.append(hex.charAt(RANDOM.nextInt(16)));
        return sb.toString();
    }
}
