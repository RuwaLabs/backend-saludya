package com.ruwalabs.saludya.iam.infrastructure.notifications;

import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories.NotificationOutboxJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import java.time.Clock;
import java.util.UUID;
@Service
public class NotificationDeliveryService {
    private final NotificationOutboxJpaRepository outbox;private final NotificationCipher cipher;
    private final JavaMailSender mail;private final Clock clock;private final String from;
    public NotificationDeliveryService(NotificationOutboxJpaRepository outbox,NotificationCipher cipher,JavaMailSender mail,
            Clock clock,@Value("${iam.notifications.from}") String from) { this.outbox=outbox;this.cipher=cipher;this.mail=mail;this.clock=clock;this.from=from; }
    @Transactional
    public void deliver(UUID id) {
        var n=outbox.lockById(id).orElseThrow();
        if(!n.getStatus().equals("PENDING")||n.getNextAttemptAt().isAfter(clock.instant())) return;
        n.setAttempts(n.getAttempts()+1);
        try {
            var message=new SimpleMailMessage();message.setFrom(from);message.setTo(n.getRecipient());message.setSubject(n.getSubject());
            message.setText(cipher.decrypt(n.getEncryptedBody()));mail.send(message);
            n.setStatus("SENT");n.setSentAt(clock.instant());n.setEncryptedBody(cipher.encrypt("[Delivered]"));
        } catch(MailException ex) {
            n.setNextAttemptAt(clock.instant().plusSeconds(Math.min(3600,60L*n.getAttempts())));
            if(n.getAttempts()>=10) n.setStatus("FAILED");
        }
        outbox.save(n);
    }
}
