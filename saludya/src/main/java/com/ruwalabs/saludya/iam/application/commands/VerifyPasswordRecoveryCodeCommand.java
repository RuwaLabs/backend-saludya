package com.ruwalabs.saludya.iam.application.commands;

public record VerifyPasswordRecoveryCodeCommand(String challengeId, String code) { }
