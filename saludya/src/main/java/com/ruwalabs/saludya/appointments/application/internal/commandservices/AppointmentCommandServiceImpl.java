package com.ruwalabs.saludya.appointments.application.internal.commandservices;

import com.ruwalabs.saludya.appointments.application.commands.BookAppointmentCommand;
import com.ruwalabs.saludya.appointments.application.commands.CancelAppointmentCommand;
import com.ruwalabs.saludya.appointments.application.commandservices.AppointmentCommandService;
import com.ruwalabs.saludya.appointments.domain.model.aggregates.Appointment;
import com.ruwalabs.saludya.appointments.domain.model.repositories.AppointmentRepository;
import com.ruwalabs.saludya.appointments.domain.model.repositories.DoctorRepository;
import com.ruwalabs.saludya.appointments.domain.model.repositories.TimeSlotRepository;
import com.ruwalabs.saludya.appointments.domain.model.valueobjects.BookingOrder;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link AppointmentCommandService}.
 */
@Service
public class AppointmentCommandServiceImpl implements AppointmentCommandService {

    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentCommandServiceImpl(
            AppointmentRepository appointmentRepository,
            TimeSlotRepository timeSlotRepository,
            DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    @Transactional
    public Result<Appointment, ApplicationError> book(BookAppointmentCommand command) {
        var timeSlot = timeSlotRepository.findById(command.timeSlotId()).orElse(null);
        if (timeSlot == null) {
            return Result.failure(ApplicationError.notFound("TimeSlot", command.timeSlotId().toString()));
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
        if (!appointment.isActive()) {
            return Result.failure(ApplicationError.conflict("Appointment",
                    "Appointment %s is not active".formatted(command.appointmentId())));
        }

        appointment.cancel();

        timeSlotRepository.findById(appointment.getTimeSlotId()).ifPresent(timeSlot -> {
            timeSlot.decrementBookings();
            timeSlotRepository.save(timeSlot);
        });

        var saved = appointmentRepository.save(appointment);
        return Result.success(saved);
    }
}
