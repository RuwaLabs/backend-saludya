package com.ruwalabs.saludya.iam.application.results;

import java.time.Instant;

public record PasswordRecoveryChallengeResult(String challengeId, String maskedEmail, Instant expiresAt) { }
