package com.ruwalabs.saludya.iam.application.internal;

import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import com.ruwalabs.saludya.iam.domain.services.SmsGateway;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates and verifies short-lived SMS codes for phone verification.
 *
 * <p>Codes are stored in memory with a five-minute TTL. This supports phone
 * verification during registration, login and password changes.</p>
 */
@Service
public class PhoneVerificationService {

    private static final Duration TTL = Duration.ofMinutes(5);
    private static final SecureRandom RANDOM = new SecureRandom();

    private record VerificationCode(String code, Instant expiresAt) {
    }

    private final Map<String, VerificationCode> codes = new ConcurrentHashMap<>();
    private final SmsGateway smsGateway;
    private final Clock clock;

    public PhoneVerificationService(SmsGateway smsGateway, Clock clock) {
        this.smsGateway = smsGateway;
        this.clock = clock;
    }

    /**
     * Generates a code, stores it and sends it by SMS.
     *
     * @param phone the phone number to verify
     */
    public void requestCode(String phone) {
        requireValidPhone(phone);
        var code = "%06d".formatted(RANDOM.nextInt(1_000_000));
        codes.put(phone, new VerificationCode(code, clock.instant().plus(TTL)));
        smsGateway.send(phone, "Tu codigo de verificacion de SaludYa es: " + code + " (vence en 5 minutos)");
    }

    /**
     * Verifies a code for a phone number.
     *
     * @param phone the phone number
     * @param code  the received code
     * @return {@code true} if the code is valid and not expired
     */
    public boolean verify(String phone, String code) {
        requireValidPhone(phone);
        var stored = codes.get(phone);
        if (stored == null || clock.instant().isAfter(stored.expiresAt())) {
            return false;
        }
        if (!stored.code().equals(code)) {
            return false;
        }
        codes.remove(phone);
        return true;
    }

    private void requireValidPhone(String phone) {
        if (phone == null || !phone.matches("\\+?[0-9]{9,15}")) {
            throw new IamException(422, "IAM_INVALID_PHONE", "Provide a valid phone number");
        }
    }
}
