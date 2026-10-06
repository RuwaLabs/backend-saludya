package com.ruwalabs.saludya.iam.application.internal;

import com.ruwalabs.saludya.iam.domain.services.IdentityGateway;
import com.ruwalabs.saludya.iam.domain.services.IdentityGateway.OfficialIdentity;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Dni;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import org.springframework.stereotype.Service;
import java.text.Normalizer;
import java.time.*;
import java.util.*;

@Service
public class IdentityVerificationService {
    private final IdentityGateway gateway; private final Clock clock;
    public IdentityVerificationService(IdentityGateway gateway,Clock clock) { this.gateway=gateway;this.clock=clock; }

    /**
     * Confirms that the DNI exists and that the provided full name matches the official record.
     * The provider may not expose the birth date, so only the name is cross-checked; the birth
     * date declared at registration is used solely to determine adulthood.
     */
    public OfficialIdentity verify(String dni,String name,String lastname) {
        var identity=gateway.lookup(new Dni(dni));
        if (identity == null || !dni.equals(identity.dni().value()) || !sameFullName(identity,name,lastname))
            throw new IamException(422,"IAM_IDENTITY_MISMATCH","The identity information does not match the DNI");
        return identity;
    }

    /** Returns whether the DNI is known to the configured provider without disclosing any data. */
    public boolean exists(String dni) {
        try { gateway.lookup(new Dni(dni)); return true; }
        catch (IamException ex) {
            if ("IAM_DNI_NOT_FOUND".equals(ex.getCode())) return false;
            throw ex;
        }
    }

    public LocalDate today() { return LocalDate.now(clock.withZone(ZoneId.of("America/Lima"))); }

    public void requireAdult(LocalDate birthDate) {
        if (birthDate == null || birthDate.plusYears(18).isAfter(today()))
            throw new IamException(422,"IAM_ADULT_REQUIRED","An account holder must be at least 18 years old");
    }

    /**
     * Permissive simulation for linking minors: if the DNI is a known fixture it is used as
     * is; otherwise the provided data is treated as an existing minor. A provider outage is
     * never masked as a synthetic identity.
     */
    public OfficialIdentity lookupOrSynthesizeMinor(String dni,String name,String lastname,LocalDate birthDate) {
        OfficialIdentity identity;
        try {
            identity = gateway.lookup(new Dni(dni));
        } catch (IamException ex) {
            if (!"IAM_DNI_NOT_FOUND".equals(ex.getCode())) throw ex;
            identity = new OfficialIdentity(new Dni(dni), name, lastname, birthDate, Set.of());
        }
        var effectiveBirthDate = identity.birthDate() != null ? identity.birthDate() : birthDate;
        if (effectiveBirthDate == null || !effectiveBirthDate.plusYears(18).isAfter(today()))
            throw new IamException(422,"IAM_MINOR_REQUIRED","The beneficiary must be younger than 18");
        return new OfficialIdentity(identity.dni(), identity.name(), identity.lastname(), effectiveBirthDate,
                identity.guardianDnis() == null ? Set.of() : identity.guardianDnis());
    }

    private boolean sameFullName(OfficialIdentity identity,String name,String lastname) {
        var official=nameTokens(identity.name(),identity.lastname());
        var provided=nameTokens(name,lastname);
        return !official.isEmpty() && official.equals(provided);
    }

    private Set<String> nameTokens(String name,String lastname) {
        var normalized=normalize(((name == null ? "" : name)+" "+(lastname == null ? "" : lastname)));
        if (normalized.isBlank()) return Set.of();
        return new TreeSet<>(List.of(normalized.split("\\s+")));
    }

    private String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value,Normalizer.Form.NFD).replaceAll("\\p{M}","").strip()
                .replaceAll("\\s+"," ").toUpperCase(Locale.ROOT);
    }
}
