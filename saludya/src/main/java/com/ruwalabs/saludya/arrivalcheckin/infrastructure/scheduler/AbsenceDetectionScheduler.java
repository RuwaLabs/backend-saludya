package com.ruwalabs.saludya.arrivalcheckin.infrastructure.scheduler;

import com.ruwalabs.saludya.arrivalcheckin.application.commandservices.CheckInCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Background scheduler for the {@code Arrival & QR Check-in} bounded context.
 *
 * <p>Periodically detects called patients that exceeded the post-call tolerance and
 * declares them absent, which publishes {@code PatientAbsentEvent} and triggers the
 * reassignment protocol.</p>
 */
@Component
@Slf4j
public class AbsenceDetectionScheduler {

    private final CheckInCommandService checkInCommandService;

    public AbsenceDetectionScheduler(CheckInCommandService checkInCommandService) {
        this.checkInCommandService = checkInCommandService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void detectAbsences() {
        var detected = checkInCommandService.detectAbsences();
        if (detected > 0) {
            log.info("Absence detection marked {} queue entries as absent", detected);
        }
    }
}
