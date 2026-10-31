package com.ruwalabs.saludya.iam.infrastructure.configuration;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.ruwalabs.saludya.iam.domain.model.aggregates.UserAccount;
import com.ruwalabs.saludya.iam.domain.model.entities.StaffProfile;
import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Dni;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Email;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.PhoneNumber;
import com.ruwalabs.saludya.iam.domain.repositories.*;
import com.ruwalabs.saludya.iam.domain.services.HashingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Explicitly enabled, idempotent provisioning for preconfigured admission staff. */
@Component
@ConditionalOnProperty(name = "iam.staff-seed.enabled", havingValue = "true")
public class AdmissionStaffSeed implements ApplicationRunner {
    private final UserAccountRepository users;
    private final StaffProfileRepository staff;
    private final PatientRepository patients;
    private final IdentityClaimRepository claims;
    private final HashingService hashing;
    private final Clock clock;
    private final ObjectMapper mapper;
    private final String accountsJson;

    public AdmissionStaffSeed(UserAccountRepository users, StaffProfileRepository staff, PatientRepository patients,
            IdentityClaimRepository claims, HashingService hashing, Clock clock, ObjectMapper mapper,
            @Value("${iam.staff-seed.accounts:}") String accountsJson) {
        this.users = users;
        this.staff = staff;
        this.patients = patients;
        this.claims = claims;
        this.hashing = hashing;
        this.clock = clock;
        this.mapper = mapper;
        this.accountsJson = accountsJson;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (accountsJson == null || accountsJson.isBlank()) {
            throw new IllegalStateException("IAM_STAFF_SEED_ACCOUNTS must contain the staff account JSON.");
        }
        List<Map<String, String>> accounts = mapper.readValue(accountsJson, new TypeReference<>() {});
        if (accounts.isEmpty()) throw new IllegalStateException("Admission staff seed has no accounts.");
        for (Map<String, String> account : accounts) provision(account);
    }

    private void provision(Map<String, String> account) {
        var email = new Email(required(account, "email"));
        var dni = new Dni(required(account, "dni"));
        var name = required(account, "name");
        var lastname = required(account, "lastname");
        var birthDate = LocalDate.parse(required(account, "birthDate"));
        var phone = new PhoneNumber(required(account, "phone")).value();
        var password = account.get("password");
        if (password == null || password.isBlank())
            throw new IllegalStateException("Missing 'password' in admission staff seed entry.");

        var existing = users.findByEmail(email.value());
        if (existing.isPresent()) {
            if (existing.get().getRole() != Role.ADMISSION_STAFF)
                throw new IllegalStateException("Staff seed email belongs to a different role.");
            var existingProfile = staff.findByUserId(existing.get().getId())
                    .orElseThrow(() -> new IllegalStateException("Existing admission staff account has no staff profile."));
            if (!existingProfile.dni().value().equals(dni.value()))
                throw new IllegalStateException("Staff seed email is already associated with a different DNI.");
            return; // Never reset credentials or overwrite a provisioned profile on restart.
        }
        if (staff.findByDni(dni.value()).isPresent() || patients.findByDni(dni.value()).isPresent())
            throw new IllegalStateException("Staff seed DNI is already registered.");
        if (birthDate.plusYears(18).isAfter(LocalDate.now(clock)))
            throw new IllegalStateException("Admission staff seed account holder must be an adult.");

        var user = users.save(new UserAccount(null, email, hashing.hash(password), Role.ADMISSION_STAFF, true, clock.instant()));
        claims.claim(dni.value(), user.getId());
        staff.save(new StaffProfile(null, user.getId(), dni, name, lastname, birthDate, phone));
    }

    private static String required(Map<String, String> account, String key) {
        var value = account.get(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("Missing '" + key + "' in admission staff seed entry.");
        return value.trim();
    }
}
