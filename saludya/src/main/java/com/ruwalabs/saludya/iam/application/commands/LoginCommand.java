package com.ruwalabs.saludya.iam.application.commands;

public record LoginCommand(String email, String dni, String password, com.ruwalabs.saludya.iam.domain.model.enums.Role role) {
    @Override public String toString() { return "LoginCommand[REDACTED]"; }
}
