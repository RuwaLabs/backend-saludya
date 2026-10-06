package com.ruwalabs.saludya.iam.application.internal;

import com.ruwalabs.saludya.iam.application.results.LoginChallengeResult;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Issues and validates short-lived email login challenges for two-step authentication.
 *
 * <p>After a successful password check the account holder receives a one-time code by email and
 * must present the matching challenge to obtain a session. Challenges are single-use and expire
 * after fifteen minutes; the code itself is validated by {@link EmailVerificationService}.</p>
 */
@Service
public class LoginChallengeService {

    private static final Duration TTL = Duration.ofMinutes(15);
    private static final String PURPOSE = "login";
    private static final String SUBJECT = "Código de inicio de sesión de SaludYa";
    private static final String MESSAGE = "Tu código para iniciar sesión en SaludYa es: {code}";

    private record Challenge(Long userId, String email, Instant expiresAt) {
    }

    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();
    private final EmailVerificationService emailVerification;
    private final Clock clock;

    public LoginChallengeService(EmailVerificationService emailVerification, Clock clock) {
        this.emailVerification = emailVerification;
        this.clock = clock;
    }

    public LoginChallengeResult issue(Long userId, String email) {
        var challengeId = UUID.randomUUID().toString();
        var expiresAt = clock.instant().plus(TTL);
        challenges.put(challengeId, new Challenge(userId, email, expiresAt));
        emailVerification.requestCode(PURPOSE, email, SUBJECT, MESSAGE);
        return new LoginChallengeResult(challengeId, maskEmail(email), expiresAt);
    }

    public void resend(String challengeId) {
        var challenge = require(challengeId);
        emailVerification.requestCode(PURPOSE, challenge.email(), SUBJECT, MESSAGE);
    }

    /** Validates the code and returns the account id, consuming the challenge on success. */
    public Long consume(String challengeId, String code) {
        var challenge = require(challengeId);
        if (clock.instant().isAfter(challenge.expiresAt())) {
            challenges.remove(challengeId);
            throw new IamException(422, "IAM_LOGIN_CHALLENGE_EXPIRED", "The verification code has expired");
        }
        emailVerification.verify(PURPOSE, challenge.email(), code);
        challenges.remove(challengeId);
        return challenge.userId();
    }

    private Challenge require(String challengeId) {
        var challenge = challengeId == null ? null : challenges.get(challengeId);
        if (challenge == null)
            throw new IamException(422, "IAM_INVALID_LOGIN_CHALLENGE", "The login challenge is invalid or expired");
        return challenge;
    }

    private static String maskEmail(String email) {
        var at = email.indexOf('@');
        if (at <= 0) return "***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}
