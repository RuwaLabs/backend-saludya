package com.ruwalabs.saludya.iam.application.commands;

public record ChangePasswordCommand(Long userId, String currentPassword, String password, String confirmPassword) {
    @Override public String toString() { return "ChangePasswordCommand[REDACTED]"; }
}
