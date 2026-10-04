package com.ruwalabs.saludya.iam.infrastructure.notifications;

import com.ruwalabs.saludya.iam.domain.services.NotificationGateway;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.entities.NotificationOutboxEntity;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories.NotificationOutboxJpaRepository;
import org.springframework.stereotype.Service;
import java.time.Clock;
@Service
public class NotificationAdapter implements NotificationGateway {
    private final NotificationOutboxJpaRepository outbox;private final NotificationCipher cipher;private final Clock clock;
    public NotificationAdapter(NotificationOutboxJpaRepository outbox,NotificationCipher cipher,Clock clock) { this.outbox=outbox;this.cipher=cipher;this.clock=clock; }
    public void enqueue(String recipient,String subject,String body) {
        var n=new NotificationOutboxEntity();n.setRecipient(recipient);n.setSubject(subject);n.setEncryptedBody(cipher.encrypt(body));
        n.setCreatedAt(clock.instant());n.setNextAttemptAt(clock.instant());outbox.save(n);
    }
}
