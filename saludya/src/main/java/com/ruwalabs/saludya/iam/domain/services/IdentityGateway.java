package com.ruwalabs.saludya.iam.domain.services;

import com.ruwalabs.saludya.iam.domain.model.valueobjects.Dni;
import java.time.LocalDate;
import java.util.Set;
public interface IdentityGateway {
    OfficialIdentity lookup(Dni dni);
    record OfficialIdentity(Dni dni, String name, String lastname, LocalDate birthDate, Set<String> guardianDnis) {}
}
