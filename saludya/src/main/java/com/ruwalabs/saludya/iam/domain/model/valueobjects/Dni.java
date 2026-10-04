package com.ruwalabs.saludya.iam.domain.model.valueobjects;

public record Dni(String value) {
    public Dni {
        if (value == null || !value.matches("[0-9]{8}"))
            throw new IllegalArgumentException("DNI must contain exactly 8 digits");
    }
}
