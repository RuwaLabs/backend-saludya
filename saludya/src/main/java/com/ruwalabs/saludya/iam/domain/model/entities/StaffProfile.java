package com.ruwalabs.saludya.iam.domain.model.entities;

import com.ruwalabs.saludya.iam.domain.model.valueobjects.*;
import java.time.LocalDate;
public record StaffProfile(Long id, Long userId, Dni dni, String name, String lastname,
                           LocalDate birthDate, String phone) {
    public StaffProfile { new PhoneNumber(phone); }
    public StaffProfile updatePhone(String phone) {
        return new StaffProfile(id,userId,dni,name,lastname,birthDate,new PhoneNumber(phone).value());
    }
}
