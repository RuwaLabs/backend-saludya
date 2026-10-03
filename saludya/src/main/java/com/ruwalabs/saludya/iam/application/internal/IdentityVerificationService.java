package com.ruwalabs.saludya.iam.application.internal;

import com.ruwalabs.saludya.iam.domain.services.IdentityGateway;
import com.ruwalabs.saludya.iam.domain.services.IdentityGateway.OfficialIdentity;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Dni;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import org.springframework.stereotype.Service;
import java.text.Normalizer;
import java.time.*;
import java.util.Locale;
@Service
public class IdentityVerificationService {
    private final IdentityGateway gateway; private final Clock clock;
    public IdentityVerificationService(IdentityGateway gateway,Clock clock) { this.gateway=gateway;this.clock=clock; }
    public OfficialIdentity verify(String dni,String name,String lastname,LocalDate birthDate) {
        var identity=gateway.lookup(new Dni(dni));
        if (identity == null || !dni.equals(identity.dni().value()) || identity.birthDate() == null
                || identity.birthDate().isAfter(today()) || !normalize(identity.name()).equals(normalize(name))
                || !normalize(identity.lastname()).equals(normalize(lastname)) || !identity.birthDate().equals(birthDate))
            throw new IamException(422,"IAM_IDENTITY_MISMATCH","The identity information does not match the DNI");
        return identity;
    }
    public LocalDate today() { return LocalDate.now(clock.withZone(ZoneId.of("America/Lima"))); }
    public void requireAdult(OfficialIdentity i) {
        if (i.birthDate().plusYears(18).isAfter(today()))
            throw new IamException(422,"IAM_ADULT_REQUIRED","An account holder must be at least 18 years old");
    }
    private String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value,Normalizer.Form.NFD).replaceAll("\\p{M}","").strip()
                .replaceAll("\\s+"," ").toUpperCase(Locale.ROOT);
    }
}
