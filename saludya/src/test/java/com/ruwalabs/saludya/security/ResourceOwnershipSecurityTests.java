package com.ruwalabs.saludya.security;

import com.ruwalabs.saludya.iam.domain.model.aggregates.UserAccount;
import com.ruwalabs.saludya.iam.domain.model.entities.StaffProfile;
import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Dni;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Email;
import com.ruwalabs.saludya.iam.domain.repositories.IdentityClaimRepository;
import com.ruwalabs.saludya.iam.domain.repositories.StaffProfileRepository;
import com.ruwalabs.saludya.iam.domain.repositories.UserAccountRepository;
import com.ruwalabs.saludya.iam.domain.services.HashingService;
import com.ruwalabs.saludya.iam.infrastructure.notifications.NotificationCipher;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories.NotificationOutboxJpaRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cross-patient ownership (IDOR) regression tests.
 *
 * <p>Verifies that a patient cannot read or act on another patient's appointments,
 * check-ins, queue entries or reassignment offers, and that the appointment listing
 * requires an explicit {@code patientId} for patients.</p>
 */
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ResourceOwnershipSecurityTests {

    private static final String ACCOUNTS = "/api/v1/user-accounts";
    private static final String APPOINTMENTS = "/api/v1/appointments";
    private static final String CHECK_INS = "/api/v1/check-ins";
    private static final String QUEUE_ENTRIES = "/api/v1/queue-entries";
    private static final String OFFERS = "/api/v1/reassignment-offers";
    private static final String PASSWORD = "Patient1!Password";
    private static final String SLOT_DATE = LocalDate.now(ZoneId.of("America/Lima")).plusDays(1).toString();

    private final JsonMapper json = JsonMapper.builder().build();

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserAccountRepository users;
    @Autowired StaffProfileRepository staff;
    @Autowired IdentityClaimRepository claims;
    @Autowired HashingService hashing;
    @Autowired TransactionTemplate tx;
    @Autowired NotificationOutboxJpaRepository outbox;
    @Autowired NotificationCipher cipher;
    @MockitoBean JavaMailSender mail;

    @BeforeEach void cleanDatabase() {
        for (String table : List.of("reassignment_offers", "queue_entries", "check_ins", "attendance_queues",
                "appointments", "time_slots", "account_recovery_requests", "iam_notification_outboxes",
                "password_reset_tokens", "user_sessions", "patient_minors", "identity_claims", "staff_profiles",
                "patients", "users")) {
            jdbc.update("DELETE FROM " + table);
        }
    }

    // ---------- helpers ----------

    private ResultActions postJson(String path, Object body, String token) throws Exception {
        var request = post(path).contentType("application/json").content(json.writeValueAsString(body));
        if (token != null) request.header("Authorization", "Bearer " + token);
        return mvc.perform(request);
    }

    private String body(ResultActions result) throws Exception { return result.andReturn().getResponse().getContentAsString(); }
    private Long id(String response, String path) { return ((Number) JsonPath.read(response, path)).longValue(); }
    private String text(String response, String path) { return JsonPath.read(response, path); }

    private String emailCode(String email, String subjectFragment) {
        return outbox.findAll().stream()
                .filter(n -> n.getRecipient().equals(email))
                .filter(n -> n.getSubject().contains(subjectFragment))
                .sorted(Comparator.comparing(n -> n.getCreatedAt()))
                .map(n -> cipher.decrypt(n.getEncryptedBody()))
                .map(s -> Pattern.compile("\\b(\\d{6})\\b").matcher(s))
                .filter(Matcher::find).map(m -> m.group(1))
                .reduce((a, b) -> b).orElseThrow(() -> new IllegalStateException("No email code for " + email));
    }

    private String login(String email, String password) throws Exception {
        var start = body(postJson(ACCOUNTS + "/login", Map.of("email", email, "password", password), null).andExpect(status().isOk()));
        var challengeId = text(start, "$.challengeId");
        var code = emailCode(email, "inicio de sesión");
        return text(body(postJson(ACCOUNTS + "/login/verify", Map.of("challengeId", challengeId, "code", code), null)
                .andExpect(status().isOk())), "$.accessToken");
    }

    private String registerAndLogin(String dni, String name, String lastname, String birth, String email) throws Exception {
        postJson(ACCOUNTS + "/send-verification-code", Map.of("email", email), null).andExpect(status().isAccepted());
        var registrationCode = emailCode(email, "Verifica tu correo");
        var body = new HashMap<String, Object>();
        body.put("dni", dni); body.put("name", name); body.put("lastname", lastname);
        body.put("birthDate", birth); body.put("phone", "987654321"); body.put("email", email);
        body.put("password", PASSWORD); body.put("code", registrationCode);
        postJson(ACCOUNTS, body, null).andExpect(status().isCreated());
        return login(email, PASSWORD);
    }

    private String admin() throws Exception {
        tx.executeWithoutResult(s -> {
            var a = users.save(new UserAccount(null, new Email("admin@example.test"), hashing.hash(PASSWORD), Role.SUPER_ADMIN, true, Instant.now()));
            claims.claim("30000001", a.getId());
            staff.save(new StaffProfile(null, a.getId(), new Dni("30000001"), "Carlos", "Vega", LocalDate.parse("1980-01-12"), "987654321"));
        });
        return login("admin@example.test", PASSWORD);
    }

    private Long patientId(String token) throws Exception {
        return id(body(mvc.perform(get(ACCOUNTS + "/me").header("Authorization", "Bearer " + token)).andExpect(status().isOk())), "$.patientId");
    }

    private Long createSlot(String admin, String start, String end) throws Exception {
        var r = body(postJson("/api/v1/time-slots", Map.of("doctorId", 1, "date", SLOT_DATE, "startHour", start, "endHour", end, "maxCapacity", 2), admin)
                .andExpect(status().isCreated()));
        return id(r, "$.id");
    }

    private Long book(String token, Long patientId, Long slotId) throws Exception {
        var r = body(postJson(APPOINTMENTS, Map.of("patientId", patientId, "timeSlotId", slotId), token).andExpect(status().isCreated()));
        return id(r, "$.id");
    }

    private String qrToken(String token, Long appointmentId) throws Exception {
        return text(body(mvc.perform(get(CHECK_INS + "/appointment/" + appointmentId + "/qr-token")
                .header("Authorization", "Bearer " + token)).andExpect(status().isOk())), "$.qrToken");
    }

    private Long checkIn(String admin, String qrToken) throws Exception {
        var r = body(postJson(CHECK_INS + "/qr", Map.of("qrToken", qrToken), admin).andExpect(status().isCreated()));
        return id(r, "$.queueEntryId");
    }

    // ---------- tests ----------

    @Test void listingAppointmentsRequiresPatientIdForPatients() throws Exception {
        var a = registerAndLogin("71234821", "Lucía", "Torres", "1995-04-15", "a@example.test");
        mvc.perform(get(APPOINTMENTS).header("Authorization", "Bearer " + a)).andExpect(status().isBadRequest());
        mvc.perform(get(APPOINTMENTS + "?doctorId=1").header("Authorization", "Bearer " + a)).andExpect(status().isBadRequest());
    }

    @Test void patientCannotReadOrCancelAnotherPatientsAppointment() throws Exception {
        var a = registerAndLogin("71234821", "Lucía", "Torres", "1995-04-15", "a@example.test");
        var b = registerAndLogin("87654321", "Diego", "Ramos", "1990-06-20", "b@example.test");
        var admin = admin();
        var slot = createSlot(admin, "09:00", "09:30");
        var apptB = book(b, patientId(b), slot);

        mvc.perform(get(APPOINTMENTS + "?patientId=" + patientId(b)).header("Authorization", "Bearer " + a)).andExpect(status().isForbidden());
        mvc.perform(get(APPOINTMENTS + "/" + apptB).header("Authorization", "Bearer " + a)).andExpect(status().isForbidden());
        mvc.perform(delete(APPOINTMENTS + "/" + apptB).header("Authorization", "Bearer " + a)).andExpect(status().isForbidden());

        mvc.perform(get(APPOINTMENTS + "/" + apptB).header("Authorization", "Bearer " + b)).andExpect(status().isOk());
        mvc.perform(get(APPOINTMENTS + "?patientId=" + patientId(b)).header("Authorization", "Bearer " + b)).andExpect(status().isOk());
    }

    @Test void patientCannotReadAnotherPatientsCheckInOrQueueEntry() throws Exception {
        var a = registerAndLogin("71234821", "Lucía", "Torres", "1995-04-15", "a@example.test");
        var b = registerAndLogin("87654321", "Diego", "Ramos", "1990-06-20", "b@example.test");
        var admin = admin();
        var slot = createSlot(admin, "09:00", "09:30");
        var apptB = book(b, patientId(b), slot);
        var queueEntryB = checkIn(admin, qrToken(b, apptB));

        mvc.perform(get(CHECK_INS + "/appointment/" + apptB).header("Authorization", "Bearer " + a)).andExpect(status().isForbidden());
        mvc.perform(get(CHECK_INS + "/appointment/" + apptB + "/qr-token").header("Authorization", "Bearer " + a)).andExpect(status().isForbidden());
        mvc.perform(get(QUEUE_ENTRIES + "/" + queueEntryB).header("Authorization", "Bearer " + a)).andExpect(status().isForbidden());
        mvc.perform(post(QUEUE_ENTRIES + "/" + queueEntryB + "/leave").header("Authorization", "Bearer " + a)).andExpect(status().isForbidden());

        mvc.perform(get(QUEUE_ENTRIES + "/" + queueEntryB).header("Authorization", "Bearer " + b)).andExpect(status().isOk());
    }

    @Test void qrTokenFailsClosedForUnknownAppointment() throws Exception {
        var a = registerAndLogin("71234821", "Lucía", "Torres", "1995-04-15", "a@example.test");
        mvc.perform(get(CHECK_INS + "/appointment/999999/qr-token").header("Authorization", "Bearer " + a)).andExpect(status().isForbidden());
    }

    @Test void patientCannotRespondToAnotherPatientsReassignmentOffer() throws Exception {
        var a = registerAndLogin("71234821", "Lucía", "Torres", "1995-04-15", "a@example.test");
        var b = registerAndLogin("87654321", "Diego", "Ramos", "1990-06-20", "b@example.test");
        var admin = admin();
        var slotA = createSlot(admin, "09:00", "09:30");
        var slotB = createSlot(admin, "11:00", "11:30");
        var apptA = book(a, patientId(a), slotA);
        book(b, patientId(b), slotB);

        var queueEntryA = checkIn(admin, qrToken(a, apptA));
        postJson(QUEUE_ENTRIES + "/" + queueEntryA + "/absent", Map.of(), admin).andExpect(status().isNoContent());

        var pending = body(mvc.perform(get(OFFERS + "/pending").header("Authorization", "Bearer " + b)).andExpect(status().isOk()));
        var offerId = id(pending, "$[0].id");

        mvc.perform(post(OFFERS + "/" + offerId + "/accept").header("Authorization", "Bearer " + a)).andExpect(status().isForbidden());
        mvc.perform(post(OFFERS + "/" + offerId + "/reject").header("Authorization", "Bearer " + a)).andExpect(status().isForbidden());

        mvc.perform(post(OFFERS + "/" + offerId + "/accept").header("Authorization", "Bearer " + b)).andExpect(status().isOk());
    }
}
