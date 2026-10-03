package com.ruwalabs.saludya.appointments.domain.model.aggregates;

import java.util.Objects;

/**
 * Entity representing a medical specialty within the catalog.
 *
 * <p>Kept under {@code aggregates} following the module's packaging convention,
 * although it is a plain entity (not an aggregate root) since it does not
 * enforce invariants of its own.</p>
 */
public class Specialty {

    private Long id;
    private final String name;
    private final String description;

    private Specialty(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static Specialty create(String name, String description) {
        Objects.requireNonNull(name, "name cannot be null");
        return new Specialty(null, name, description);
    }

    public static Specialty rehydrate(Long id, String name, String description) {
        return new Specialty(id, name, description);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
