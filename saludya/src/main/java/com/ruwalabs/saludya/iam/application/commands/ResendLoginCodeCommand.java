package com.ruwalabs.saludya.iam.application.commands;

public record ResendLoginCodeCommand(String challengeId) {
    @Override public String toString() { return "ResendLoginCodeCommand[REDACTED]"; }
}
