package com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.services;

import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.application.commands.UpdateConfigurationCommand;
import com.ruwalabs.saludya.hospitaloperations.hospitalconfiguration.domain.model.aggregates.HospitalConfiguration;

/**
 * Application service responsible for configuration commands.
 */
public interface ConfigurationCommandService {

    HospitalConfiguration updateConfiguration(
            UpdateConfigurationCommand command
    );
}