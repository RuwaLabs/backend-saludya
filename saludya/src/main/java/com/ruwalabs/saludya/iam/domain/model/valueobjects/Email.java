package com.ruwalabs.saludya.iam.domain.model.valueobjects;

import java.util.Locale;
public record Email(String value) {
    public Email {
        if (value == null) throw new IllegalArgumentException("Email is required");
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() > 150 || !value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("Email is invalid");
    }
}
