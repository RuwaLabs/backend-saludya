package com.ruwalabs.saludya.iam.application.internal;

import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import com.ruwalabs.saludya.iam.domain.services.NotificationGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates and verifies short-lived email codes for registration and login.
 *
 * <p>Codes are stored in memory, scoped by purpose and email so a registration code cannot be
 * replayed for login, expire after fifteen minutes, are single-use and allow a bounded number
 * of attempts. Messages are queued through the notification outbox so delivery can retry.</p>
 */
@Service
@Slf4j
public class EmailVerificationService {

    private static final Duration TTL = Duration.ofMinutes(15);
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private record Code(String value, Instant expiresAt, int attempts) {
    }

    private final Map<String, Code> codes = new ConcurrentHashMap<>();
    private final NotificationGateway notifications;
    private final Clock clock;
    private final boolean logCodes;

    public EmailVerificationService(NotificationGateway notifications, Clock clock,
                                    @Value("${iam.notifications.log-codes:false}") boolean logCodes) {
        this.notifications = notifications;
        this.clock = clock;
        this.logCodes = logCodes;
    }

    /**
     * Generates a code and queues it to the recipient. The message body may contain a
     * {@code {code}} placeholder that is replaced with the generated value.
     */
    public void requestCode(String purpose, String email, String subject, String message) {
        var code = "%06d".formatted(RANDOM.nextInt(1_000_000));
        codes.put(key(purpose, email), new Code(code, clock.instant().plus(TTL), 0));
        notifications.enqueue(email, subject, message.replace("{code}", code) + "\nEl código vence en 15 minutos.");
        if (logCodes) log.info("[EMAIL][{}] verification code for {}: {}", purpose, email, code);
    }

    /** Validates a code, consuming it on success. */
    public void verify(String purpose, String email, String code) {
        var key = key(purpose, email);
        var stored = codes.get(key);
        if (stored == null) throw invalidCode();
        if (clock.instant().isAfter(stored.expiresAt())) {
            codes.remove(key);
            throw new IamException(422, "IAM_EMAIL_CODE_EXPIRED", "The verification code has expired");
        }
        if (!stored.value().equals(code)) {
            var attempts = stored.attempts() + 1;
            if (attempts >= MAX_ATTEMPTS) {
                codes.remove(key);
                throw new IamException(429, "IAM_EMAIL_TOO_MANY_ATTEMPTS", "Too many invalid verification attempts");
            }
            codes.put(key, new Code(stored.value(), stored.expiresAt(), attempts));
            throw invalidCode();
        }
        codes.remove(key);
    }

    private static IamException invalidCode() {
        return new IamException(422, "IAM_INVALID_EMAIL_CODE", "The verification code is invalid or expired");
    }

    private static String key(String purpose, String email) {
        return purpose + "|" + email.toLowerCase(Locale.ROOT);
    }
}
