package com.ruwalabs.saludya.iam;

import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import com.ruwalabs.saludya.iam.domain.repositories.StaffProfileRepository;
import com.ruwalabs.saludya.iam.domain.repositories.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:staffseedtest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "iam.staff-seed.enabled=true",
        "iam.staff-seed.accounts=[{\"email\":\"seed@example.test\",\"dni\":\"70000003\",\"name\":\"Test\",\"lastname\":\"Staff\",\"birthDate\":\"1990-01-01\",\"phone\":\"900000000\",\"password\":\"StaffPassword1\"}]"
})
class AdmissionStaffSeedIntegrationTests {
    @Autowired private UserAccountRepository users;
    @Autowired private StaffProfileRepository staff;

    @Test
    void startsWithSeedEnabledAndProvisionsAdmissionStaff() {
        var account = users.findByEmail("seed@example.test").orElseThrow();
        assertEquals(Role.ADMISSION_STAFF, account.getRole());
        assertTrue(staff.findByUserId(account.getId()).isPresent());
    }
}
