package com.ruwalabs.saludya.iam.application.internal.commandservices;

import com.ruwalabs.saludya.iam.application.commands.*;
import com.ruwalabs.saludya.iam.application.commandservices.UserAccountCommandService;
import com.ruwalabs.saludya.iam.application.results.*;
import com.ruwalabs.saludya.iam.application.internal.*;
import com.ruwalabs.saludya.iam.application.internal.queryservices.UserAccountQueryServiceImpl;
import com.ruwalabs.saludya.iam.domain.repositories.*;
import com.ruwalabs.saludya.iam.domain.services.*;
import com.ruwalabs.saludya.iam.domain.model.aggregates.*;
import com.ruwalabs.saludya.iam.domain.model.entities.*;
import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import com.ruwalabs.saludya.iam.domain.model.events.*;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import com.ruwalabs.saludya.iam.domain.model.factories.UserAccountFactory;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import java.time.*;
import java.util.*;
@Service @Transactional
public class UserAccountCommandServiceImpl implements UserAccountCommandService {
    private final UserAccountRepository users;private final PatientRepository patients;private final StaffProfileRepository staff;
    private final SessionRepository sessions;private final HashingService hashing;private final TokenService tokens;
    private final IdentityVerificationService identities;private final AccessPolicy access;private final EventPublisher events;
    private final PasswordRecoveryService recovery;private final UserAccountQueryServiceImpl profiles;private final Clock clock;
    private final long expirationMs;private final PasswordHash dummyHash;
    private final IdentityClaimRepository identityClaims;
    private final StaffEmailPolicy staffEmailPolicy;
    public UserAccountCommandServiceImpl(UserAccountRepository users,PatientRepository patients,StaffProfileRepository staff,
            SessionRepository sessions,HashingService hashing,TokenService tokens,IdentityVerificationService identities,AccessPolicy access,
            EventPublisher events,PasswordRecoveryService recovery,UserAccountQueryServiceImpl profiles,Clock clock,
            @Value("${application.jwt.expiration-ms}") long expirationMs,IdentityClaimRepository identityClaims,StaffEmailPolicy staffEmailPolicy) {
        this.identityClaims=identityClaims;
        this.staffEmailPolicy=staffEmailPolicy;
        this.users=users;this.patients=patients;this.staff=staff;this.sessions=sessions;this.hashing=hashing;this.tokens=tokens;
        this.identities=identities;this.access=access;this.events=events;this.recovery=recovery;this.profiles=profiles;this.clock=clock;
        if(expirationMs<60000||expirationMs>86400000) throw new IllegalArgumentException("JWT lifetime must be between 1 minute and 24 hours");
        this.expirationMs=expirationMs;this.dummyHash=hashing.hash("Nonexistent1!Account");
    }
    public Patient registerPatient(RegisterPatientCommand c) {
        var identity=identities.verify(c.dni(),c.name(),c.lastname(),c.birthDate());identities.requireAdult(identity);
        var email=new Email(c.email());ensureEmailAvailable(email,null);ensureIdentityAvailable(c.dni());
        var user=users.save(UserAccountFactory.createPatientAccount(email,hashing.hash(c.password()),clock.instant()));
        identityClaims.claim(identity.dni().value(),user.getId());
        var patient=patients.save(new Patient(null,user.getId(),identity.dni(),identity.name(),identity.lastname(),identity.birthDate(),c.phone()));
        events.publish(new PatientRegisteredEvent(user.getId(),patient.getId()));return patient;
    }
    public AccountProfile createStaffAccount(CreateStaffAccountCommand c) {
        access.requireSuperAdmin(c.actorId());var identity=identities.verify(c.dni(),c.name(),c.lastname(),c.birthDate());identities.requireAdult(identity);
        var email=new Email(c.email());staffEmailPolicy.requireCorporateEmail(email);ensureEmailAvailable(email,null);ensureIdentityAvailable(c.dni());
        var user=users.save(UserAccountFactory.createStaffAccount(email,hashing.hash("Sy1!"+UUID.randomUUID()),clock.instant()));
        identityClaims.claim(identity.dni().value(),user.getId());
        staff.save(new StaffProfile(null,user.getId(),identity.dni(),identity.name(),identity.lastname(),identity.birthDate(),c.phone()));
        recovery.sendStaffInvitation(user);events.publish(new StaffAccountCreatedEvent(user.getId()));return profiles.profile(user.getId());
    }
    public AuthResult login(LoginCommand c) {
        boolean hasEmail=c.email()!=null&&!c.email().isBlank(),hasDni=c.dni()!=null&&!c.dni().isBlank();
        if (hasEmail==hasDni||c.role()==null) throw new IllegalArgumentException("Provide email or DNI, password and account role");
        UserAccount account=null;
        if (hasEmail) account=users.findByEmail(new Email(c.email()).value()).orElse(null);
        else {
            new Dni(c.dni());
            Long id=patients.findByDni(c.dni()).map(Patient::getUserId)
                    .orElseGet(()->staff.findByDni(c.dni()).map(StaffProfile::userId).orElse(null));
            if (id!=null) account=users.findById(id).orElse(null);
        }
        boolean valid=hashing.matches(c.password(),account==null?dummyHash:account.getPassword());
        if (!valid||account==null||!account.isActive()||account.getRole()!=c.role()) throw IamException.unauthorized();
        UUID id=UUID.randomUUID();Instant expires=clock.instant().plusMillis(expirationMs).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        sessions.save(new UserSession(id,account.getId(),expires,null));
        return new AuthResult(tokens.issue(account,id,expires),"Bearer",expires,account.getId(),account.getRole(),
                patients.findByUserId(account.getId()).map(Patient::getId).orElse(null));
    }
    public void logout(LogoutCommand c) { sessions.revoke(c.sessionId(),clock.instant()); }
    public void recoverPassword(RecoverPasswordCommand c) { recovery.request(c.email()); }
    public void resetPassword(ResetPasswordCommand c) { recovery.reset(c.token(),c.password(),c.confirmPassword()); }
    public void changePassword(ChangePasswordCommand c) {
        access.requireSelf(c.userId(),c.userId());
        var user=users.lockById(c.userId()).orElseThrow(IamException::notFound);
        if (!hashing.matches(c.currentPassword(),user.getPassword())) throw IamException.unauthorized();
        if(!Objects.equals(c.password(),c.confirmPassword())) throw new IamException(422,"IAM_PASSWORD_MISMATCH","Passwords do not match");
        user.changePassword(hashing.hash(c.password()));users.save(user);sessions.revokeByUserId(user.getId(),clock.instant());
        recovery.invalidateOutstandingLinks(user.getId());
        events.publish(new ProfileUpdatedEvent(user.getId(),user.getEmail().value()));
    }
    public AccountProfile updateProfile(UpdateProfileCommand c) {
        access.requireSelf(c.actorId(),c.userId());
        var user=users.lockById(c.userId()).orElseThrow(IamException::notFound);
        var email=new Email(c.email());new PhoneNumber(c.phone());ensureEmailAvailable(email,user.getId());
        String old=user.getEmail().value();user.updateEmail(email);users.save(user);
        if (user.getRole()==Role.PATIENT) {
            var p=patients.findByUserId(user.getId()).orElseThrow(IamException::notFound);
            p.updateContactInfo(c.phone());patients.save(p);
        } else {
            var p=staff.findByUserId(user.getId()).orElseThrow(IamException::notFound);
            staff.save(p.updatePhone(c.phone()));
        }
        events.publish(new ProfileUpdatedEvent(user.getId(),old));return profiles.profile(user.getId());
    }
    private void ensureEmailAvailable(Email email,Long self) {
        if(users.findByEmail(email.value()).filter(u->!u.getId().equals(self)).isPresent())
            throw new IamException(409,"IAM_EMAIL_ALREADY_REGISTERED","The email is already registered");
    }
    private void ensureIdentityAvailable(String dni) {
        if(patients.findByDni(dni).isPresent()||staff.findByDni(dni).isPresent())
            throw new IamException(409,"IAM_DNI_ALREADY_REGISTERED","The DNI is already registered");
    }
}
