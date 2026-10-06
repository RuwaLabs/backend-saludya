package com.ruwalabs.saludya.iam.application.commands;

public record VerifyLoginCommand(String challengeId, String code) {
    @Override public String toString() { return "VerifyLoginCommand[REDACTED]"; }
}
