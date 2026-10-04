package com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.Appointment;
import com.ruwalabs.saludya.appointments.domain.model.events.AppointmentBookedEvent;
import com.ruwalabs.saludya.appointments.domain.model.repositories.AppointmentRepository;
import com.ruwalabs.saludya.appointments.domain.model.valueobjects.AppointmentStatus;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities.AppointmentJpaEntity;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.mappers.AppointmentPersistenceMapper;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.repositories.SpringDataAppointmentJpaRepository;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.repositories.SpringDataDoctorJpaRepository;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.repositories.SpringDataTimeSlotJpaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Comparator;
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
    private final SpringDataTimeSlotJpaRepository timeSlotRepository;
    private final SpringDataDoctorJpaRepository doctorRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AppointmentPersistenceAdapter(
            SpringDataAppointmentJpaRepository repository,
            SpringDataTimeSlotJpaRepository timeSlotRepository,
            SpringDataDoctorJpaRepository doctorRepository,
            ApplicationEventPublisher applicationEventPublisher) {
        this.repository = repository;
        this.timeSlotRepository = timeSlotRepository;
        this.doctorRepository = doctorRepository;
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
    public List<Appointment> findFiltered(
            Long patientId,
            Long timeSlotId,
            Long doctorId,
            Long specialtyId,
            java.time.LocalDate date,
            AppointmentStatus status) {
        return repository.findAll().stream()
                .filter(entity -> patientId == null || patientId.equals(entity.getPatientId()))
                .filter(entity -> timeSlotId == null || timeSlotId.equals(entity.getTimeSlotId()))
                .filter(entity -> status == null || status == entity.getStatus())
                .filter(entity -> matchesSlotFilters(entity, doctorId, specialtyId, date))
                .sorted(Comparator
                        .comparing((com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities.AppointmentJpaEntity e)
                                -> slotDate(e))
                        .thenComparing(e -> e.getBookingOrder()))
                .map(AppointmentPersistenceMapper::toDomain)
                .toList();
    }

    private boolean matchesSlotFilters(
            com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities.AppointmentJpaEntity entity,
            Long doctorId,
            Long specialtyId,
            java.time.LocalDate date) {
        if (doctorId == null && specialtyId == null && date == null) {
            return true;
        }
        var slot = timeSlotRepository.findById(entity.getTimeSlotId()).orElse(null);
        if (slot == null) {
            return false;
        }
        if (date != null && !date.equals(slot.getDate())) {
            return false;
        }
        if (doctorId != null && !doctorId.equals(slot.getDoctorId())) {
            return false;
        }
        if (specialtyId != null) {
            var doctor = doctorRepository.findById(slot.getDoctorId()).orElse(null);
            if (doctor == null || !specialtyId.equals(doctor.getSpecialtyId())) {
                return false;
            }
        }
        return true;
    }

    private java.time.LocalDate slotDate(
            com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities.AppointmentJpaEntity entity) {
        return timeSlotRepository.findById(entity.getTimeSlotId())
                .map(com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities.TimeSlotJpaEntity::getDate)
                .orElse(java.time.LocalDate.MIN);
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
