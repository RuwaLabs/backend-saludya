package com.ruwalabs.saludya.appointments.application.internal.commandservices;

import com.ruwalabs.saludya.appointments.application.commands.BookAppointmentCommand;
import com.ruwalabs.saludya.appointments.application.commands.CancelAppointmentCommand;
import com.ruwalabs.saludya.appointments.application.commandservices.AppointmentCommandService;
import com.ruwalabs.saludya.appointments.application.internal.outboundservices.acl.HospitalConfigurationService;
import com.ruwalabs.saludya.appointments.application.internal.outboundservices.acl.PatientAccessService;
import com.ruwalabs.saludya.appointments.domain.model.aggregates.Appointment;
import com.ruwalabs.saludya.appointments.domain.model.repositories.AppointmentRepository;
import com.ruwalabs.saludya.appointments.domain.model.repositories.DoctorRepository;
import com.ruwalabs.saludya.appointments.domain.model.repositories.TimeSlotRepository;
import com.ruwalabs.saludya.appointments.domain.model.valueobjects.BookingOrder;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Implementation of {@link AppointmentCommandService}.
 */
@Service
public class AppointmentCommandServiceImpl implements AppointmentCommandService {

    private static final ZoneId ZONE = ZoneId.of("America/Lima");

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalConfigurationService hospitalConfigurationService;
    private final PatientAccessService patientAccessService;

    public AppointmentCommandServiceImpl(
            AppointmentRepository appointmentRepository,
            TimeSlotRepository timeSlotRepository,
            DoctorRepository doctorRepository,
            HospitalConfigurationService hospitalConfigurationService,
            PatientAccessService patientAccessService) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.doctorRepository = doctorRepository;
        this.hospitalConfigurationService = hospitalConfigurationService;
        this.patientAccessService = patientAccessService;
    }

    @Override
    @Transactional
    public Result<Appointment, ApplicationError> book(BookAppointmentCommand command) {
        if (!patientAccessService.canManagePatient(command.patientId())) {
            return Result.failure(ApplicationError.forbidden(
                    "Appointment", "You cannot book an appointment for this patient"));
        }
        var timeSlot = timeSlotRepository.findById(command.timeSlotId()).orElse(null);
        if (timeSlot == null) {
            return Result.failure(ApplicationError.notFound("TimeSlot", command.timeSlotId().toString()));
        }
        if (timeSlot.isExpired()) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "time-slot-expired", "The selected time slot is in the past"));
        }
        if (isAfterBookingCutoff(timeSlot.getDate())) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "booking-cutoff", "Booking is closed for today"));
        }
        if (!timeSlot.hasAvailableCapacity()) {
            return Result.failure(ApplicationError.conflict("TimeSlot",
                    "Time slot %s has no available capacity".formatted(command.timeSlotId())));
        }
        if (appointmentRepository.existsActiveByPatientAndTimeSlot(command.patientId(), command.timeSlotId())) {
            return Result.failure(ApplicationError.conflict("Appointment",
                    "Patient %s already has an active appointment in time slot %s"
                            .formatted(command.patientId(), command.timeSlotId())));
        }
        var doctor = doctorRepository.findById(timeSlot.getDoctorId()).orElse(null);
        if (doctor == null) {
            return Result.failure(ApplicationError.notFound("Doctor", timeSlot.getDoctorId().toString()));
        }

        int nextBookingOrder = appointmentRepository.getNextBookingOrderBySpecialty(doctor.getSpecialtyId());
        var appointment = Appointment.create(
                command.timeSlotId(),
                command.patientId(),
                new BookingOrder(nextBookingOrder));

        timeSlot.incrementBookings();
        timeSlotRepository.save(timeSlot);

        var saved = appointmentRepository.save(appointment);
        return Result.success(saved);
    }

    @Override
    @Transactional
    public Result<Appointment, ApplicationError> cancel(CancelAppointmentCommand command) {
        var appointment = appointmentRepository.findById(command.appointmentId()).orElse(null);
        if (appointment == null) {
            return Result.failure(ApplicationError.notFound("Appointment", command.appointmentId().toString()));
        }
        if (!patientAccessService.canManagePatient(appointment.getPatientId())) {
            return Result.failure(ApplicationError.forbidden(
                    "Appointment", "You cannot cancel this appointment"));
        }
        if (!appointment.isActive()) {
            return Result.failure(ApplicationError.conflict("Appointment",
                    "Appointment %s is not active".formatted(command.appointmentId())));
        }

        var timeSlot = timeSlotRepository.findById(appointment.getTimeSlotId()).orElse(null);
        if (timeSlot != null) {
            var scheduledAt = toInstant(timeSlot.getDate(), timeSlot.getStartHour());
            if (!appointment.canBeCancelled(scheduledAt, hospitalConfigurationService.cancellationDeadlineHours())) {
                return Result.failure(ApplicationError.businessRuleViolation(
                        "cancellation-deadline",
                        "The cancellation deadline has passed; cancel in person with admission staff"));
            }
        }

        appointment.cancel();

        if (timeSlot != null) {
            timeSlot.decrementBookings();
            timeSlotRepository.save(timeSlot);
        }

        var saved = appointmentRepository.save(appointment);
        return Result.success(saved);
    }

    private boolean isAfterBookingCutoff(LocalDate slotDate) {
        LocalTime cutoff = hospitalConfigurationService.bookingCutoffTime();
        if (cutoff == null) {
            return false;
        }
        var today = LocalDate.now(ZONE);
        return slotDate.equals(today) && LocalTime.now(ZONE).isAfter(cutoff);
    }

    private Instant toInstant(LocalDate date, LocalTime time) {
        return date.atTime(time).atZone(ZONE).toInstant();
    }
}
