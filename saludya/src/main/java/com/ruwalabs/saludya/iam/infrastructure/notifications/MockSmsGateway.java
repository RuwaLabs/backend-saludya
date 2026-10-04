package com.ruwalabs.saludya.iam.infrastructure.notifications;

import com.ruwalabs.saludya.iam.domain.services.SmsGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Mock SMS gateway.
 *
 * <p>Instead of calling a real SMS provider, it logs the message (which contains the
 * verification code) so the flow can be exercised locally. Replace with a real
 * provider adapter when needed.</p>
 */
@Service
@Slf4j
public class MockSmsGateway implements SmsGateway {

    @Override
    public void send(String phone, String message) {
        log.info("[SMS][mock] to {}: {}", phone, message);
    }
}
