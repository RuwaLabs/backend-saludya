package com.ruwalabs.saludya.arrivalcheckin.infrastructure.notifications;

import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Notification adapter for the Arrival context.
 *
 * <p>Check-in and call notifications are delivered through the push/SMS channel,
 * which is mocked (logged). Methods are asynchronous and never throw.</p>
 */
@Service
@Slf4j
public class ArrivalNotificationAdapter implements NotificationService {

    @Async
    @Override
    public void notifyCheckInCompleted(Long appointmentId, int position) {
        log.info("[PUSH][mock] Check-in completed for appointment {}; queue position {}",
                appointmentId, position);
    }

    @Async
    @Override
    public void notifyPatientCalled(Long queueEntryId, int position) {
        log.info("[PUSH][mock] Queue entry {} called at position {}", queueEntryId, position);
    }
}
