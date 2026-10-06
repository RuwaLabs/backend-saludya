package com.ruwalabs.saludya.iam.infrastructure.configuration;

import com.ruwalabs.saludya.iam.domain.model.aggregates.UserAccount;
import com.ruwalabs.saludya.iam.domain.model.entities.StaffProfile;
import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Dni;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Email;
import com.ruwalabs.saludya.iam.domain.repositories.*;
import com.ruwalabs.saludya.iam.domain.services.HashingService;
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
    private final HashingService hashing;private final Clock clock;private final IdentityClaimRepository identityClaims;
    private final String email,password,dni,name,lastname,birthDate,phone;
    public SuperAdminBootstrap(UserAccountRepository users,StaffProfileRepository staff,PatientRepository patients,
            HashingService hashing,Clock clock,IdentityClaimRepository identityClaims,
            @Value("${iam.bootstrap.email}") String email,@Value("${iam.bootstrap.password}") String password,
            @Value("${iam.bootstrap.dni}") String dni,@Value("${iam.bootstrap.name}") String name,
            @Value("${iam.bootstrap.lastname}") String lastname,@Value("${iam.bootstrap.birth-date}") String birthDate,
            @Value("${iam.bootstrap.phone}") String phone) {
        this.identityClaims=identityClaims;
        this.users=users;this.staff=staff;this.patients=patients;this.hashing=hashing;this.clock=clock;
        this.email=email;this.password=password;this.dni=dni;this.name=name;this.lastname=lastname;this.birthDate=birthDate;this.phone=phone;
    }
    @Override @Transactional public void run(ApplicationArguments args) {
        var canonicalEmail=new Email(email);
        var existing=users.findByEmail(canonicalEmail.value());
        if(existing.isPresent()) {
            if(existing.get().getRole()!=Role.SUPER_ADMIN) throw new IllegalStateException("Bootstrap email belongs to another role");
            return; // Do not reset credentials on subsequent starts.
        }
        var birth=LocalDate.parse(birthDate);
        if(birth.plusYears(18).isAfter(LocalDate.now(clock))) throw new IllegalStateException("Bootstrap account holder must be an adult");
        if(staff.findByDni(dni).isPresent()||patients.findByDni(dni).isPresent()) throw new IllegalStateException("Bootstrap DNI is already registered");
        var user=users.save(new UserAccount(null,canonicalEmail,hashing.hash(password),Role.SUPER_ADMIN,true,clock.instant()));
        identityClaims.claim(dni,user.getId());
        staff.save(new StaffProfile(null,user.getId(),new Dni(dni),name,lastname,birth,phone));
    }
}
