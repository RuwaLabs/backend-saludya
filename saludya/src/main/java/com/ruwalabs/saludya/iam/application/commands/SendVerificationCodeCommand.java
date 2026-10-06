package com.ruwalabs.saludya.iam.application.commands;

public record SendVerificationCodeCommand(String email) {
    @Override public String toString() { return "SendVerificationCodeCommand[REDACTED]"; }
}
