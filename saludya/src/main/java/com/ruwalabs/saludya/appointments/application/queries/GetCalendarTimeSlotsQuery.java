package com.ruwalabs.saludya.appointments.application.queries;

import java.time.LocalDate;

public record GetCalendarTimeSlotsQuery(Long specialtyId, LocalDate from, LocalDate to) {
}
