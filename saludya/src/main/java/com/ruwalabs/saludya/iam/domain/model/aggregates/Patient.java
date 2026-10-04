package com.ruwalabs.saludya.iam.domain.model.aggregates;

import com.ruwalabs.saludya.iam.domain.model.valueobjects.*;
import com.ruwalabs.saludya.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import java.time.*;
import java.util.Objects;
@Getter
public class Patient extends AbstractDomainAggregateRoot<Patient> {
    private final Long id;
    private final Long userId;
    private final Dni dni;
    private final String name;
    private final String lastname;
    private final LocalDate birthDate;
    private String phone;
    public Patient(Long id, Long userId, Dni dni, String name, String lastname, LocalDate birthDate, String phone) {
        this.id=id; this.userId=userId; this.dni=Objects.requireNonNull(dni);
        if (name == null || name.isBlank() || lastname == null || lastname.isBlank())
            throw new IllegalArgumentException("Verified names are required");
        this.name=name; this.lastname=lastname; this.birthDate=Objects.requireNonNull(birthDate);
        this.phone=phone == null ? null : new PhoneNumber(phone).value();
    }
    public boolean isMinor(LocalDate today) { return birthDate.plusYears(18).isAfter(today); }
    public void updateContactInfo(String phone) { this.phone = new PhoneNumber(phone).value(); }
}
