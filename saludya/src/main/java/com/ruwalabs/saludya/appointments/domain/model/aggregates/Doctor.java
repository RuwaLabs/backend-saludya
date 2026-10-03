package com.ruwalabs.saludya.appointments.domain.model.aggregates;

import java.util.Objects;

/**
 * Entity representing a doctor within the medical catalog.
 *
 * <p>Kept under {@code aggregates} following the module's packaging convention,
 * although it is a plain entity (not an aggregate root) since it does not
 * enforce invariants of its own.</p>
 */
public class Doctor {

    private Long id;
    private final Long specialtyId;
    private final String name;
    private final String lastname;

    private Doctor(Long id, Long specialtyId, String name, String lastname) {
        this.id = id;
        this.specialtyId = specialtyId;
        this.name = name;
        this.lastname = lastname;
    }

    public static Doctor create(Long specialtyId, String name, String lastname) {
        Objects.requireNonNull(specialtyId, "specialtyId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(lastname, "lastname cannot be null");
        return new Doctor(null, specialtyId, name, lastname);
    }

    public static Doctor rehydrate(Long id, Long specialtyId, String name, String lastname) {
        return new Doctor(id, specialtyId, name, lastname);
    }

    public Long getId() {
        return id;
    }

    public Long getSpecialtyId() {
        return specialtyId;
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }
}
