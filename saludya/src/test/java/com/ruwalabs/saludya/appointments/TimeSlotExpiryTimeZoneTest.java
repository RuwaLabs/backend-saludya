package com.ruwalabs.saludya.appointments;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.TimeSlot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeSlotExpiryTimeZoneTest {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Lima");

    @Test
    void upcomingLimaSlotDoesNotExpireWhenServerUsesAnotherTimeZone() {
        var now = LocalDateTime.now(BUSINESS_ZONE);
        var end = now.withSecond(0).withNano(0).plusMinutes(30);
        Assumptions.assumeTrue(end.toLocalDate().equals(now.toLocalDate()));
        var slot = TimeSlot.create(
                1L,
                end.toLocalDate(),
                end.minusMinutes(30).toLocalTime(),
                end.toLocalTime(),
                "Consultorio 01",
                8);
        var previousTimeZone = TimeZone.getDefault();

        try {
            // Kiritimati is UTC+14, ahead of Lima and often on the next date.
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"));
            assertFalse(slot.isExpired());
        } finally {
            TimeZone.setDefault(previousTimeZone);
        }
    }

    @Test
    void slotThatEndedInLimaIsExpired() {
        var end = LocalDateTime.now(BUSINESS_ZONE).minusMinutes(30);
        var slot = TimeSlot.create(
                1L,
                end.toLocalDate(),
                end.minusMinutes(30).toLocalTime(),
                end.toLocalTime(),
                "Consultorio 01",
                8);

        assertTrue(slot.isExpired());
    }
}
