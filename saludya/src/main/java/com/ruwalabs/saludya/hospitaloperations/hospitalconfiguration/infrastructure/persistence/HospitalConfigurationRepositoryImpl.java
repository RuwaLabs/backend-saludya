package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.infrastructure.persistence;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.model.aggregates.HospitalConfiguration;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.repositories.HospitalConfigurationRepository;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.infrastructure.persistence.entities.HospitalConfigurationEntity;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.infrastructure.persistence.repositories.SpringDataHospitalConfigurationRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * PostgreSQL/JPA implementation of the domain repository.
 */
@Repository
@Transactional
public class HospitalConfigurationRepositoryImpl
        implements HospitalConfigurationRepository {

    private final SpringDataHospitalConfigurationRepository repository;

    public HospitalConfigurationRepositoryImpl(
            SpringDataHospitalConfigurationRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public HospitalConfiguration save(
            HospitalConfiguration configuration
    ) {
        HospitalConfigurationEntity entity =
                toEntity(configuration);

        HospitalConfigurationEntity saved =
                repository.save(entity);

        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HospitalConfiguration> findDefault() {
        return repository.findFirstByOrderByIdAsc()
                .map(this::toDomain);
    }

    @Override
    public HospitalConfiguration update(
            HospitalConfiguration configuration
    ) {
        if (configuration.getId() == null) {
            return save(configuration);
        }

        HospitalConfigurationEntity existing =
                repository.findById(configuration.getId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Hospital configuration with id "
                                        + configuration.getId()
                                        + " was not found"
                        ));

        HospitalConfigurationEntity updated =
                new HospitalConfigurationEntity(
                        existing.getId(),
                        configuration.getMaxCapacityPerSlot(),
                        configuration.getBookingOrderScope(),
                        configuration.getCheckInToleranceMinutes(),
                        configuration.getPostCallToleranceMinutes(),
                        configuration.getReassignmentResponseTimeoutMin(),
                        configuration.getBookingCutoffTime(),
                        configuration.getCancellationDeadlineHours(),
                        configuration.isAttendanceQueueVisible(),
                        configuration.getUpdatedAt()
                );

        return toDomain(repository.save(updated));
    }

    private HospitalConfigurationEntity toEntity(
            HospitalConfiguration configuration
    ) {
        return new HospitalConfigurationEntity(
                configuration.getId(),
                configuration.getMaxCapacityPerSlot(),
                configuration.getBookingOrderScope(),
                configuration.getCheckInToleranceMinutes(),
                configuration.getPostCallToleranceMinutes(),
                configuration.getReassignmentResponseTimeoutMin(),
                configuration.getBookingCutoffTime(),
                configuration.getCancellationDeadlineHours(),
                configuration.isAttendanceQueueVisible(),
                configuration.getUpdatedAt()
        );
    }

    private HospitalConfiguration toDomain(
            HospitalConfigurationEntity entity
    ) {
        return HospitalConfiguration.reconstitute(
                entity.getId(),
                entity.getMaxCapacityPerSlot(),
                entity.getBookingOrderScope(),
                entity.getCheckInToleranceMinutes(),
                entity.getPostCallToleranceMinutes(),
                entity.getReassignmentResponseTimeoutMin(),
                entity.getBookingCutoffTime(),
                entity.getCancellationDeadlineHours(),
                entity.isAttendanceQueueVisible(),
                entity.getUpdatedAt()
        );
    }
}