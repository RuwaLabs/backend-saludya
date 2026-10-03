package com.ruwalabs.saludya.appointments.interfaces.rest;

import com.ruwalabs.saludya.appointments.application.queryservices.SpecialtyQueryService;
import com.ruwalabs.saludya.appointments.application.queries.GetAllSpecialtiesQuery;
import com.ruwalabs.saludya.appointments.application.queries.GetSpecialtyByIdQuery;
import com.ruwalabs.saludya.appointments.interfaces.rest.resources.SpecialtyResource;
import com.ruwalabs.saludya.appointments.interfaces.rest.transform.SpecialtyResourceFromEntityAssembler;
import com.ruwalabs.saludya.shared.application.result.ApplicationError;
import com.ruwalabs.saludya.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for the medical specialty catalog.
 */
@RestController
@RequestMapping(value = "/api/v1/specialties", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Specialties", description = "Medical specialty catalog endpoints")
public class SpecialtiesController {

    private final SpecialtyQueryService queryService;

    public SpecialtiesController(SpecialtyQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    public ResponseEntity<List<SpecialtyResource>> getAllSpecialties() {
        var specialties = queryService.getAll(new GetAllSpecialtiesQuery());
        var resources = specialties.stream()
                .map(SpecialtyResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSpecialtyById(@PathVariable Long id) {
        var specialty = queryService.getById(new GetSpecialtyByIdQuery(id));
        if (specialty.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Specialty", id.toString()));
        }
        return ResponseEntity.ok(
                SpecialtyResourceFromEntityAssembler.toResourceFromEntity(specialty.get()));
    }
}
