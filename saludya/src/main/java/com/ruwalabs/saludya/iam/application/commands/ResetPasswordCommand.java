package com.ruwalabs.saludya.iam.application.commands;

public record ResetPasswordCommand(String token, String password, String confirmPassword) {
    @Override public String toString() { return "ResetPasswordCommand[REDACTED]"; }
}
