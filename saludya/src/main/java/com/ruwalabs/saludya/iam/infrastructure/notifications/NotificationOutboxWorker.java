package com.ruwalabs.saludya.iam.infrastructure.notifications;

import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories.NotificationOutboxJpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.time.Clock;
@Component @ConditionalOnProperty(name="iam.notifications.delivery-enabled",havingValue="true")
public class NotificationOutboxWorker {
    private final NotificationOutboxJpaRepository outbox;private final NotificationDeliveryService delivery;private final Clock clock;
    public NotificationOutboxWorker(NotificationOutboxJpaRepository outbox,NotificationDeliveryService delivery,Clock clock) { this.outbox=outbox;this.delivery=delivery;this.clock=clock; }
    @Scheduled(fixedDelayString="${iam.notifications.poll-ms:5000}")
    public void poll() {
        for(var n:outbox.findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAt("PENDING",clock.instant())) delivery.deliver(n.getId());
    }
}
