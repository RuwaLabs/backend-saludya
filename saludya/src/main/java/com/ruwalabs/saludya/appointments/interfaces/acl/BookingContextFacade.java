package com.ruwalabs.saludya.appointments.interfaces.acl;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.Appointment;
import com.ruwalabs.saludya.appointments.domain.model.repositories.AppointmentRepository;
import com.ruwalabs.saludya.appointments.domain.model.repositories.DoctorRepository;
import com.ruwalabs.saludya.appointments.domain.model.repositories.TimeSlotRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Optional;

/**
 * Facade that other bounded contexts use to read and change appointment data
 * without reaching into the {@code Appointments & Booking} repositories.
 *
 * <p>It is the provider side of the ACL used by {@code Arrival & QR Check-in} and
 * {@code Reassignment}.</p>
 */
@Service
public class BookingContextFacade {

    private static final ZoneId ZONE = ZoneId.of("America/Lima");

    /**
     * Appointment data exposed to other bounded contexts.
     *
     * @param id         the appointment identifier
     * @param timeSlotId the time slot identifier
     * @param patientId  the patient identifier
     * @param status     the appointment status
     * @param slotStart  the instant the time slot starts (nullable)
     */
    public record AppointmentInfo(
            Long id,
            Long timeSlotId,
            Long patientId,
            String status,
            Instant slotStart) {
    }

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final DoctorRepository doctorRepository;

    public BookingContextFacade(
            AppointmentRepository appointmentRepository,
            TimeSlotRepository timeSlotRepository,
            DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.doctorRepository = doctorRepository;
    }

    public Optional<AppointmentInfo> findAppointmentInfo(Long appointmentId) {
        return appointmentRepository.findById(appointmentId).map(this::toInfo);
    }

    /**
     * Lists the appointments of a given date (all statuses).
     *
     * @param date the date to query
     * @return the appointments of that date
     */
    public java.util.List<AppointmentInfo> findAppointmentsByDate(java.time.LocalDate date) {
        return appointmentRepository.findFiltered(null, null, null, null, date, null).stream()
                .map(this::toInfo)
                .toList();
    }

    /**
     * Finds the next candidate for a freed slot: the active appointment with the
     * lowest {@code bookingOrder} in the same specialty, excluding appointments
     * already in the freed slot and the given appointment.
     *
     * @param freedTimeSlotId     the freed time slot identifier
     * @param excludeAppointmentId an appointment to exclude (e.g. the one that just moved), nullable
     * @return the candidate appointment id, if any
     */
    public Optional<Long> findNextCandidateByBookingOrder(Long freedTimeSlotId, Long excludeAppointmentId) {
        var timeSlot = timeSlotRepository.findById(freedTimeSlotId).orElse(null);
        if (timeSlot == null) {
            return Optional.empty();
        }
        var doctor = doctorRepository.findById(timeSlot.getDoctorId()).orElse(null);
        if (doctor == null) {
            return Optional.empty();
        }
        return appointmentRepository.findActiveBySpecialtyId(doctor.getSpecialtyId()).stream()
                .filter(a -> !a.getTimeSlotId().equals(freedTimeSlotId))
                .filter(a -> excludeAppointmentId == null || !a.getId().equals(excludeAppointmentId))
                .min(Comparator.comparingInt(a -> a.getBookingOrder().value()))
                .map(Appointment::getId);
    }

    public Optional<Long> timeSlotOfAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId).map(Appointment::getTimeSlotId);
    }

    public Optional<AppointmentInfo> markPresent(Long appointmentId) {
        return mutate(appointmentId, Appointment::confirm);
    }

    public Optional<AppointmentInfo> markAttended(Long appointmentId) {
        return mutate(appointmentId, Appointment::markAsAttended);
    }

    public Optional<AppointmentInfo> markAbsent(Long appointmentId) {
        return mutate(appointmentId, Appointment::markAsAbsent);
    }

    /**
     * Moves an appointment to another time slot, adjusting the capacity of both slots.
     *
     * @param appointmentId    the appointment to move
     * @param targetTimeSlotId the destination time slot
     * @return the updated appointment info, if the move was possible
     */
    public Optional<AppointmentInfo> moveAppointment(Long appointmentId, Long targetTimeSlotId) {
        var appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return Optional.empty();
        }
        var target = timeSlotRepository.findById(targetTimeSlotId).orElse(null);
        if (target == null || !target.hasAvailableCapacity()) {
            return Optional.empty();
        }
        var previousSlotId = appointment.getTimeSlotId();
        appointment.moveTo(targetTimeSlotId);
        target.incrementBookings();
        timeSlotRepository.save(target);
        timeSlotRepository.findById(previousSlotId).ifPresent(previous -> {
            previous.decrementBookings();
            timeSlotRepository.save(previous);
        });
        return Optional.of(toInfo(appointmentRepository.save(appointment)));
    }

    private Optional<AppointmentInfo> mutate(Long appointmentId, java.util.function.Consumer<Appointment> transition) {
        var appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return Optional.empty();
        }
        transition.accept(appointment);
        return Optional.of(toInfo(appointmentRepository.save(appointment)));
    }

    private AppointmentInfo toInfo(Appointment appointment) {
        var slotStart = timeSlotRepository.findById(appointment.getTimeSlotId())
                .map(slot -> slot.getDate().atTime(slot.getStartHour()).atZone(ZONE).toInstant())
                .orElse(null);
        return new AppointmentInfo(
                appointment.getId(),
                appointment.getTimeSlotId(),
                appointment.getPatientId(),
                appointment.getStatus().name(),
                slotStart);
    }
}
