package com.ruwalabs.saludya.appointments.infrastructure.seed;

import com.ruwalabs.saludya.appointments.domain.model.aggregates.Doctor;
import com.ruwalabs.saludya.appointments.domain.model.aggregates.Specialty;
import com.ruwalabs.saludya.appointments.domain.model.repositories.DoctorRepository;
import com.ruwalabs.saludya.appointments.domain.model.repositories.SpecialtyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds a basic medical catalog (specialties and doctors) on startup when empty,
 * so administrators can create time slots and patients can book appointments.
 */
@Component
@Slf4j
public class CatalogSeeder {

    private final SpecialtyRepository specialtyRepository;
    private final DoctorRepository doctorRepository;

    public CatalogSeeder(SpecialtyRepository specialtyRepository, DoctorRepository doctorRepository) {
        this.specialtyRepository = specialtyRepository;
        this.doctorRepository = doctorRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedCatalog() {
        if (!specialtyRepository.findAll().isEmpty()) {
            return;
        }
        var general = specialtyRepository.save(Specialty.create("Medicina General", "Atención médica general"));
        var pediatria = specialtyRepository.save(Specialty.create("Pediatría", "Atención de niños y adolescentes"));
        var cardiologia = specialtyRepository.save(Specialty.create("Cardiología", "Atención del corazón"));
        var dermatologia = specialtyRepository.save(Specialty.create("Dermatología", "Atención de la piel"));

        doctorRepository.save(Doctor.create(general.getId(), "Carlos", "Vega"));
        doctorRepository.save(Doctor.create(general.getId(), "Lucía", "Torres"));
        doctorRepository.save(Doctor.create(pediatria.getId(), "María", "Vega"));
        doctorRepository.save(Doctor.create(cardiologia.getId(), "Diego", "Ramos"));
        doctorRepository.save(Doctor.create(dermatologia.getId(), "Ana", "Soto"));

        log.info("Seeded medical catalog: 4 specialties and 5 doctors");
    }
}
