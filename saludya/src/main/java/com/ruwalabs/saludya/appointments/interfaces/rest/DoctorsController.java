package com.ruwalabs.saludya.appointments.interfaces.rest;

import com.ruwalabs.saludya.appointments.application.queryservices.DoctorQueryService;
import com.ruwalabs.saludya.appointments.application.queries.GetDoctorByIdQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetDoctorsBySpecialtyQuery;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.DoctorResource;
import com.ruwalabs.saludya.appointments.interfaces.rest.transform.DoctorResourceFromEntityAssembler;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for the doctor catalog.
 */
@RestController
@RequestMapping(value = "/api/v1/doctors", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Doctors", description = "Doctor catalog endpoints")
public class DoctorsController {

    private final DoctorQueryService queryService;

    public DoctorsController(DoctorQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    public ResponseEntity<List<DoctorResource>> getDoctorsBySpecialty(@RequestParam Long specialtyId) {
        var doctors = queryService.getBySpecialty(new GetDoctorsBySpecialtyQuery(specialtyId));
        var resources = doctors.stream()
                .map(DoctorResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctorById(@PathVariable Long id) {
        var doctor = queryService.getById(new GetDoctorByIdQuery(id));
        if (doctor.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Doctor", id.toString()));
        }
        return ResponseEntity.ok(
                DoctorResourceFromEntityAssembler.toResourceFromEntity(doctor.get()));
    }
}
