package com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.repositories;

import com.ruwalabs.saludya.appointments.domain.model.valueobjects.TimeSlotStatus;
import com.ruwalabs.saludya.appointments.infrastructure.persistence.jpa.entities.TimeSlotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for {@link TimeSlotJpaEntity}.
 */
@Repository
public interface SpringDataTimeSlotJpaRepository extends JpaRepository<TimeSlotJpaEntity, Long> {

    @Query("""
            SELECT DISTINCT ts
            FROM TimeSlotJpaEntity ts, DoctorJpaEntity d
            WHERE ts.doctorId = d.id
              AND d.specialtyId = :specialtyId
              AND ts.date = :date
              AND ts.status = :status
            """)
    List<TimeSlotJpaEntity> findAvailableBySpecialty(
            @Param("specialtyId") Long specialtyId,
            @Param("date") LocalDate date,
            @Param("status") TimeSlotStatus status
    );

    List<TimeSlotJpaEntity> findByDoctorIdAndDate(Long doctorId, LocalDate date);
}
