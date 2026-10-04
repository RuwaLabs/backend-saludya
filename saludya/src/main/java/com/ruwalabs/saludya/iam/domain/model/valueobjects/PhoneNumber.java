package com.ruwalabs.saludya.iam.domain.model.valueobjects;

public record PhoneNumber(String value) {
    public PhoneNumber {
        if (value == null || !value.matches("\\+?[0-9]{9,15}"))
            throw new IllegalArgumentException("Phone must contain 9 to 15 digits");
    }
}
