package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.services;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.commands.UpdateConfigurationCommand;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.events.ConfigurationUpdatedEvent;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.model.aggregates.HospitalConfiguration;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.publishers.EventPublisher;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.repositories.HospitalConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Default implementation of the configuration command service.
 */
@Service
@Transactional
public class ConfigurationCommandServiceImpl
        implements ConfigurationCommandService {

    private final HospitalConfigurationRepository repository;
    private final EventPublisher eventPublisher;

    public ConfigurationCommandServiceImpl(
            HospitalConfigurationRepository repository,
            EventPublisher eventPublisher
    ) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public HospitalConfiguration updateConfiguration(
            UpdateConfigurationCommand command
    ) {
        if (command == null) {
            throw new IllegalArgumentException(
                    "Update configuration command cannot be null"
            );
        }

        HospitalConfiguration configuration = repository.findDefault()
                .orElseThrow(() -> new IllegalStateException(
                        "Hospital configuration was not found"
                ));

        configuration.updateMaxCapacity(
                command.maxCapacityPerSlot()
        );

        configuration.updateBookingOrderScope(
                command.bookingOrderScope()
        );

        configuration.updateTolerances(
                command.checkInToleranceMinutes(),
                command.postCallToleranceMinutes()
        );

        configuration.updateReassignmentTimeout(
                command.reassignmentResponseTimeoutMin()
        );

        configuration.updateBookingCutoffTime(
                command.bookingCutoffTime()
        );

        configuration.updateCancellationDeadline(
                command.cancellationDeadlineHours()
        );

        configuration.updateAttendanceQueueVisibility(
                command.attendanceQueueVisible()
        );

        configuration.validate();

        HospitalConfiguration updatedConfiguration =
                repository.update(configuration);

        eventPublisher.publish(
                new ConfigurationUpdatedEvent(
                        updatedConfiguration.getId(),
                        LocalDateTime.now()
                )
        );

        return updatedConfiguration;
    }
}