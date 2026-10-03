package com.ruwalabs.saludya.iam.application.results;

import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import java.time.Instant;
public record AuthResult(String accessToken, String tokenType, Instant expiresAt, Long userId, Role role, Long patientId) {}
