package com.ruwalabs.saludya.iam.application.commands;

public record LinkMinorCommand(Long actorId, String dni, String name, String lastname, java.time.LocalDate birthDate, boolean confirmFiliation) {}
