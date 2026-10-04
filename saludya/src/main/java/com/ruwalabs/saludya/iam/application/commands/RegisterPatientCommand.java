package com.ruwalabs.saludya.iam.application.commands;

public record RegisterPatientCommand(String dni, String name, String lastname, java.time.LocalDate birthDate, String phone, String email, String password) {
    @Override public String toString() { return "RegisterPatientCommand[REDACTED]"; }
}
