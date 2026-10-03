package com.ruwalabs.saludya.iam.domain.repositories;

/** Prevent one DNI from acquiring independent accounts in two different roles. */
public interface IdentityClaimRepository { void claim(String dni,Long userId); }
