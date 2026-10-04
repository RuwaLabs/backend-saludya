package com.ruwalabs.saludya.arrivalcheckin.application.internal.commandservices;

import com.ruwalabs.saludya.arrivalcheckin.application.commands.CallNextPatientCommand;
import com.ruwalabs.saludya.arrivalcheckin.application.commandservices.QueueCommandService;
import com.ruwalabs.saludya.arrivalcheckin.application.internal.outboundservices.acl.NotificationService;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.entities.QueueEntry;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.events.PatientCalledEvent;
import com.ruwalabs.saludya.arrivalcheckin.domain.model.valueobjects.QueueEntryStatus;
import com.ruwalabs.saludya.arrivalcheckin.domain.repositories.QueueEntryRepository;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.application.result.Result;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that executes attendance queue commands.
 */
@Service
public class QueueCommandServiceImpl implements QueueCommandService {

    private final QueueEntryRepository queueEntryRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    public QueueCommandServiceImpl(
            QueueEntryRepository queueEntryRepository,
            NotificationService notificationService,
            ApplicationEventPublisher eventPublisher) {
        this.queueEntryRepository = queueEntryRepository;
        this.notificationService = notificationService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Result<QueueEntry, ApplicationError> callNextPatient(CallNextPatientCommand command) {
        var entry = queueEntryRepository.findFirstByQueueAndStatusOrderByPosition(
                command.attendanceQueueId(), QueueEntryStatus.WAITING);
        if (entry.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "QueueEntry", "No waiting patients in queue " + command.attendanceQueueId()));
        }

        var queueEntry = entry.get();
        queueEntry.call();
        var saved = queueEntryRepository.save(queueEntry);

        eventPublisher.publishEvent(new PatientCalledEvent(
                saved.getId(), command.attendanceQueueId(), saved.getPosition(), saved.getCalledAt()));
        notificationService.notifyPatientCalled(saved.getId(), saved.getPosition());

        return Result.success(saved);
    }
}
