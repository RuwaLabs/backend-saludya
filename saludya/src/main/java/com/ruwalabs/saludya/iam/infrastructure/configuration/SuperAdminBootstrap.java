package com.ruwalabs.saludya.iam.infrastructure.configuration;

import com.ruwalabs.saludya.iam.domain.model.aggregates.UserAccount;
import com.ruwalabs.saludya.iam.domain.model.entities.StaffProfile;
import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Email;
import com.ruwalabs.saludya.iam.domain.repositories.*;
import com.ruwalabs.saludya.iam.domain.services.*;
import com.ruwalabs.saludya.iam.application.internal.IdentityVerificationService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
/** Explicit operator-only provisioning; no public endpoint can create SUPER_ADMIN accounts. */
@Component @ConditionalOnProperty(name="iam.bootstrap.enabled",havingValue="true")
public class SuperAdminBootstrap implements ApplicationRunner {
    private final UserAccountRepository users;private final StaffProfileRepository staff;private final PatientRepository patients;
    private final IdentityGateway identities;private final IdentityVerificationService verification;private final HashingService hashing;private final Clock clock;
    private final String email,password,dni,phone;
    private final IdentityClaimRepository identityClaims;
    public SuperAdminBootstrap(UserAccountRepository users,StaffProfileRepository staff,PatientRepository patients,IdentityGateway identities,
            IdentityVerificationService verification,HashingService hashing,Clock clock,
            @Value("${iam.bootstrap.email}") String email,@Value("${iam.bootstrap.password}") String password,
            @Value("${iam.bootstrap.dni}") String dni,@Value("${iam.bootstrap.phone}") String phone,IdentityClaimRepository identityClaims) {
        this.identityClaims=identityClaims;
        this.users=users;this.staff=staff;this.patients=patients;this.identities=identities;this.verification=verification;this.hashing=hashing;this.clock=clock;
        this.email=email;this.password=password;this.dni=dni;this.phone=phone;
    }
    @Override @Transactional public void run(ApplicationArguments args) {
        var canonicalEmail=new Email(email);
        var existing=users.findByEmail(canonicalEmail.value());
        if(existing.isPresent()) {
            if(existing.get().getRole()!=Role.SUPER_ADMIN) throw new IllegalStateException("Bootstrap email belongs to another role");
            return; // Do not reset credentials on subsequent starts.
        }
        var identity=identities.lookup(new com.ruwalabs.saludya.iam.domain.model.valueobjects.Dni(dni));verification.requireAdult(identity);
        if(staff.findByDni(dni).isPresent()||patients.findByDni(dni).isPresent()) throw new IllegalStateException("Bootstrap DNI is already registered");
        var user=users.save(new UserAccount(null,canonicalEmail,hashing.hash(password),Role.SUPER_ADMIN,true,clock.instant()));
        identityClaims.claim(identity.dni().value(),user.getId());
        staff.save(new StaffProfile(null,user.getId(),identity.dni(),identity.name(),identity.lastname(),identity.birthDate(),phone));
    }
}
