package com.ruwalabs.saludya.iam.application.commands;

public record CreateStaffAccountCommand(Long actorId, String dni, String name, String lastname, java.time.LocalDate birthDate, String phone, String email) {}
