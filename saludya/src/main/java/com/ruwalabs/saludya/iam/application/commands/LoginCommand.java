package com.ruwalabs.saludya.iam.application.commands;

public record LoginCommand(String email, String password) {
    @Override public String toString() { return "LoginCommand[REDACTED]"; }
}
