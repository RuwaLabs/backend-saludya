package com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.appointments.domain.model.valueobjects.AppointmentStatus;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities.AppointmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link AppointmentJpaEntity}.
 */
@Repository
public interface SpringDataAppointmentJpaRepository extends JpaRepository<AppointmentJpaEntity, Long> {

    List<AppointmentJpaEntity> findByPatientId(Long patientId);

    List<AppointmentJpaEntity> findByTimeSlotId(Long timeSlotId);

    @Query("""
            SELECT DISTINCT a
            FROM AppointmentJpaEntity a, TimeSlotJpaEntity ts, DoctorJpaEntity d
            WHERE a.timeSlotId = ts.id
              AND ts.doctorId = d.id
              AND d.specialtyId = :specialtyId
              AND a.status IN :activeStatuses
            """)
    List<AppointmentJpaEntity> findActiveBySpecialtyId(
            @Param("specialtyId") Long specialtyId,
            @Param("activeStatuses") List<AppointmentStatus> activeStatuses
    );

    @Query("""
            SELECT COALESCE(MAX(a.bookingOrder), 0) + 1
            FROM AppointmentJpaEntity a, TimeSlotJpaEntity ts, DoctorJpaEntity d
            WHERE a.timeSlotId = ts.id
              AND ts.doctorId = d.id
              AND d.specialtyId = :specialtyId
            """)
    Integer getNextBookingOrderBySpecialty(@Param("specialtyId") Long specialtyId);

    boolean existsByPatientIdAndTimeSlotIdAndStatusIn(
            Long patientId,
            Long timeSlotId,
            List<AppointmentStatus> statuses
    );
}
