package com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.Appointment;
import com.ruwalabs.saludya.appointments.domain.model.events.AppointmentBookedEvent;
import com.ruwalabs.saludya.appointments.domain.model.repositories.AppointmentRepository;
import com.ruwalabs.saludya.appointments.domain.model.valueobjects.AppointmentStatus;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities.AppointmentJpaEntity;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.mappers.AppointmentPersistenceMapper;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.repositories.SpringDataAppointmentJpaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter implementing {@link AppointmentRepository} with Spring Data JPA.
 */
@Component
public class AppointmentPersistenceAdapter implements AppointmentRepository {

    private static final List<AppointmentStatus> ACTIVE_STATUSES =
            List.of(AppointmentStatus.RESERVED, AppointmentStatus.CONFIRMED);

    private final SpringDataAppointmentJpaRepository repository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AppointmentPersistenceAdapter(
            SpringDataAppointmentJpaRepository repository,
            ApplicationEventPublisher applicationEventPublisher) {
        this.repository = repository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Appointment save(Appointment appointment) {
        boolean isNew = appointment.getId() == null;

        AppointmentJpaEntity entity = AppointmentPersistenceMapper.toJpaEntity(appointment);
        AppointmentJpaEntity saved = repository.save(entity);
        Appointment result = AppointmentPersistenceMapper.toDomain(saved);

        appointment.domainEvents().forEach(applicationEventPublisher::publishEvent);
        appointment.clearDomainEvents();

        if (isNew) {
            applicationEventPublisher.publishEvent(new AppointmentBookedEvent(
                    result.getId(),
                    result.getTimeSlotId(),
                    result.getPatientId(),
                    result.getBookingOrder().value(),
                    result.getCreatedAt()));
        }

        return result;
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        return repository.findById(id).map(AppointmentPersistenceMapper::toDomain);
    }

    @Override
    public List<Appointment> findByPatientId(Long patientId) {
        return repository.findByPatientId(patientId).stream()
                .map(AppointmentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findByTimeSlotId(Long timeSlotId) {
        return repository.findByTimeSlotId(timeSlotId).stream()
                .map(AppointmentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findActiveBySpecialtyId(Long specialtyId) {
        return repository.findActiveBySpecialtyId(specialtyId, ACTIVE_STATUSES).stream()
                .map(AppointmentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public int getNextBookingOrderBySpecialty(Long specialtyId) {
        Integer next = repository.getNextBookingOrderBySpecialty(specialtyId);
        return next == null ? 1 : next;
    }

    @Override
    public boolean existsActiveByPatientAndTimeSlot(Long patientId, Long timeSlotId) {
        return repository.existsByPatientIdAndTimeSlotIdAndStatusIn(
                patientId, timeSlotId, ACTIVE_STATUSES);
    }

    @Override
    public void updateStatus(Long id, AppointmentStatus status) {
        repository.findById(id).ifPresent(entity -> {
            entity.setStatus(status);
            repository.save(entity);
        });
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
