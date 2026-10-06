package com.ruwalabs.saludya.iam;

import com.ruwalabs.saludya.iam.domain.repositories.*;
import com.ruwalabs.saludya.iam.domain.services.*;
import com.ruwalabs.saludya.iam.domain.model.aggregates.*;
import com.ruwalabs.saludya.iam.domain.model.entities.*;
import com.ruwalabs.saludya.iam.domain.model.enums.Role;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Email;
import com.ruwalabs.saludya.iam.infrastructure.persistence.jpa.repositories.*;
import com.ruwalabs.saludya.iam.infrastructure.notifications.*;
import com.ruwalabs.saludya.iam.interfaces.acl.IamContextFacade;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.mail.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.junit.jupiter.api.*;
import tools.jackson.databind.json.JsonMapper;
import com.jayway.jsonpath.JsonPath;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
@Import(IamIntegrationTests.TimeConfiguration.class)
class IamIntegrationTests {
    private static final String ACCOUNTS="/api/v1/user-accounts";
    private static final String PASSWORD="Patient1!Password";
    private final JsonMapper json=JsonMapper.builder().build();
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserAccountRepository users;
    @Autowired StaffProfileRepository staff;
    @Autowired IdentityClaimRepository claims;
    @Autowired HashingService hashing;
    @Autowired TransactionTemplate tx;
    @Autowired NotificationOutboxJpaRepository outbox;
    @Autowired PasswordResetTokenJpaRepository resetTokens;
    @Autowired NotificationCipher cipher;
    @Autowired NotificationDeliveryService delivery;
    @Autowired IamContextFacade facade;
    @Autowired MutableClock clock;
    @MockitoBean JavaMailSender mail;

    static class MutableClock extends Clock {
        private final AtomicReference<Instant> time=new AtomicReference<>(Instant.parse("2026-10-03T12:00:00Z"));
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return Clock.fixed(instant(),zone); }
        public Instant instant() { return time.get(); }
        void advance(Duration d) { time.updateAndGet(t->t.plus(d)); }
        void reset() { time.set(Instant.parse("2026-10-03T12:00:00Z")); }
    }
    @TestConfiguration static class TimeConfiguration {
        @Bean @Primary MutableClock testClock() { return new MutableClock(); }
        @Bean TransactionTemplate transactions(org.springframework.transaction.PlatformTransactionManager manager) {
            return new TransactionTemplate(manager);
        }
    }
    @BeforeEach void cleanDatabase() {
        for(String table:List.of("account_recovery_requests","iam_notification_outboxes","password_reset_tokens",
                "user_sessions","patient_minors","identity_claims","staff_profiles","patients","users")) jdbc.update("DELETE FROM "+table);
        clock.reset();reset(mail);
    }
    private Map<String,Object> adult(String dni,String name,String last,String birth,String email) {
        return Map.of("dni",dni,"name",name,"lastname",last,"birthDate",birth,"phone","987654321","email",email,"password",PASSWORD);
    }
    private Map<String,Object> lucia(String email) { return adult("71234821","Lucía","Torres","1995-04-15",email); }
    private Map<String,Object> maria(String email) { return adult("70000002","María","Vega","1992-02-10",email); }
    private Map<String,Object> minor() {
        return Map.of("dni","87652716","name","Mateo","lastname","Torres","birthDate","2018-04-15","confirmFiliation",true);
    }
    private ResultActions postJson(String path,Object body,String token) throws Exception {
        var request=post(path).contentType("application/json").content(json.writeValueAsString(body));
        if(token!=null) request.header("Authorization","Bearer "+token);
        return mvc.perform(request);
    }
    private String body(ResultActions result) throws Exception { return result.andReturn().getResponse().getContentAsString(); }
    private Long id(String response,String path) { return ((Number)JsonPath.read(response,path)).longValue(); }
    private String text(String response,String path) { return JsonPath.read(response,path); }
    private String sendVerificationCode(String email) throws Exception {
        postJson(ACCOUNTS+"/send-verification-code",Map.of("email",email),null).andExpect(status().isAccepted());
        return emailCode(email.toLowerCase(java.util.Locale.ROOT),"Verifica tu correo");
    }
    private Map<String,Object> withCode(Map<String,Object> r,String email) throws Exception {
        var body=new HashMap<>(r);body.put("code",sendVerificationCode(email));return body;
    }
    private Map<String,Object> withCodeValue(Map<String,Object> r,String value) {
        var body=new HashMap<>(r);body.put("code",value);return body;
    }
    private Long register(Map<String,Object> r) throws Exception {
        var body=withCode(r,(String)r.get("email"));
        return id(body(postJson(ACCOUNTS,body,null).andExpect(status().isCreated())),"$.userId");
    }
    private String emailCode(String email,String subjectFragment) {
        return outbox.findAll().stream().filter(n->n.getRecipient().equals(email))
                .filter(n->n.getSubject().contains(subjectFragment))
                .sorted(Comparator.comparing(n->n.getCreatedAt()))
                .map(n->cipher.decrypt(n.getEncryptedBody()))
                .map(s->Pattern.compile("\\b(\\d{6})\\b").matcher(s))
                .filter(java.util.regex.Matcher::find).map(m->m.group(1))
                .reduce((a,b)->b).orElseThrow(()->new IllegalStateException("No email code for "+email+" ("+subjectFragment+")"));
    }
    private String login(String email,String password) throws Exception {
        var start=body(postJson(ACCOUNTS+"/login",Map.of("email",email,"password",password),null).andExpect(status().isOk()));
        var challengeId=text(start,"$.challengeId");var code=emailCode(email,"inicio de sesión");
        return text(body(postJson(ACCOUNTS+"/login/verify",Map.of("challengeId",challengeId,"code",code),null)
                .andExpect(status().isOk())),"$.accessToken");
    }
    private String patient() throws Exception { register(lucia("lucia@example.test"));return login("lucia@example.test",PASSWORD); }
    private String admin() throws Exception {
        tx.executeWithoutResult(s->{
            var a=users.save(new UserAccount(null,new Email("admin@example.test"),hashing.hash(PASSWORD),Role.SUPER_ADMIN,true,clock.instant()));
            claims.claim("30000001",a.getId());
            staff.save(new StaffProfile(null,a.getId(),new com.ruwalabs.saludya.iam.domain.model.valueobjects.Dni("30000001"),
                    "Carlos","Vega",LocalDate.parse("1980-01-12"),"987654321"));
        });
        return login("admin@example.test",PASSWORD);
    }
    private String recoveryToken(String email) throws Exception {
        postJson(ACCOUNTS+"/recover-password",Map.of("email",email),null).andExpect(status().isAccepted());
        return rawToken(email);
    }
    private String rawToken(String email) {
        return outbox.findAll().stream().sorted(Comparator.comparing(n->n.getCreatedAt())).filter(n->n.getRecipient().equals(email))
                .map(n->cipher.decrypt(n.getEncryptedBody())).map(s->Pattern.compile("token=([A-Za-z0-9_-]{43})").matcher(s))
                .filter(java.util.regex.Matcher::find).map(m->m.group(1)).reduce((a,b)->b).orElseThrow();
    }
    private Map<String,String> resetBody(String token) { return Map.of("token",token,"password","Updated1!Password","confirmPassword","Updated1!Password"); }

    @Test void sendingTheCodeQueuesTheVerificationEmail() throws Exception {
        postJson(ACCOUNTS+"/send-verification-code",Map.of("email","LUCIA@example.test"),null).andExpect(status().isAccepted());
        assertThat(outbox.findAll()).hasSize(1);
        assertThat(outbox.findAll().getFirst().getSubject()).contains("Verifica tu correo");
        assertThat(cipher.decrypt(outbox.findAll().getFirst().getEncryptedBody())).contains("código");
    }
    @Test void registrationHashesPasswordAndQueuesWelcomeWithoutLeakingCredentials() throws Exception {
        var result=postJson(ACCOUNTS,withCode(lucia("LUCIA@example.test"),"LUCIA@example.test"),null).andExpect(status().isCreated())
                .andExpect(jsonPath("$.dni").value("71234821")).andExpect(jsonPath("$.password").doesNotExist());
        var user=users.findById(id(body(result),"$.userId")).orElseThrow();
        assertThat(user.getEmail().value()).isEqualTo("lucia@example.test");
        assertThat(user.getRole()).isEqualTo(Role.PATIENT);
        assertThat(user.getPassword().value()).startsWith("$2").isNotEqualTo(PASSWORD);
        assertThat(hashing.matches(PASSWORD,user.getPassword())).isTrue();
        assertThat(outbox.findAll()).anySatisfy(n->assertThat(cipher.decrypt(n.getEncryptedBody())).contains("Tu correo fue verificado"));
    }
    @Test void registrationRejectsAnInvalidCodeAndCreatesNoAccount() throws Exception {
        sendVerificationCode("lucia@example.test");
        postJson(ACCOUNTS,withCodeValue(lucia("lucia@example.test"),"000000"),null).andExpect(status().isUnprocessableEntity());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM users",Integer.class)).isZero();
    }
    @Test void forgedRegistrationRoleCannotCreateAdministrator() throws Exception {
        var r=new HashMap<>(lucia("lucia@example.test"));r.put("role","SUPER_ADMIN");
        postJson(ACCOUNTS,withCode(r,"lucia@example.test"),null).andExpect(status().isCreated());
        assertThat(users.findByEmail("lucia@example.test").orElseThrow().getRole()).isEqualTo(Role.PATIENT);
    }
    @Test void identityMismatchRollsBackAllAccountAndNotificationWrites() throws Exception {
        var r=new HashMap<>(lucia("lucia@example.test"));r.put("name","Another person");r.put("code","000000");
        postJson(ACCOUNTS,r,null).andExpect(status().isUnprocessableEntity());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM users",Integer.class)).isZero();
        assertThat(outbox.count()).isZero();
    }
    @Test void unknownDniCannotPassTheDevelopmentAllowList() throws Exception {
        postJson(ACCOUNTS,withCodeValue(adult("11111111","Unknown","Person","1990-01-01","unknown@example.test"),"000000"),null)
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("IAM_DNI_NOT_FOUND"));
    }
    @Test void minorCannotRegisterAnIndependentAccount() throws Exception {
        postJson(ACCOUNTS,withCodeValue(adult("87652716","Mateo","Torres","2018-04-15","minor@example.test"),"000000"),null)
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.code").value("IAM_ADULT_REQUIRED"));
    }
    @Test void duplicateEmailIsCaseInsensitive() throws Exception {
        register(lucia("lucia@example.test"));
        postJson(ACCOUNTS,withCodeValue(maria("LUCIA@example.test"),"000000"),null).andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM users",Integer.class)).isEqualTo(1);
    }
    @Test void duplicateDniCannotCreateAnotherAccount() throws Exception {
        register(lucia("lucia@example.test"));
        postJson(ACCOUNTS,withCodeValue(lucia("other@example.test"),"000000"),null).andExpect(status().isConflict());
    }
    @Test void concurrentRegistrationsForTheSameIdentityCreateExactlyOneAccount() throws Exception {
        var a=withCode(lucia("a@example.test"),"a@example.test");
        var b=withCode(lucia("b@example.test"),"b@example.test");
        try(var pool=Executors.newFixedThreadPool(2)) {
            var start=new CountDownLatch(1);
            var r1=pool.submit(()->{ start.await();return postJson(ACCOUNTS,a,null).andReturn().getResponse().getStatus(); });
            var r2=pool.submit(()->{ start.await();return postJson(ACCOUNTS,b,null).andReturn().getResponse().getStatus(); });
            start.countDown();
            assertThat(List.of(r1.get(15,TimeUnit.SECONDS),r2.get(15,TimeUnit.SECONDS))).containsExactlyInAnyOrder(201,409);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM users",Integer.class)).isEqualTo(1);
        }
    }
    @Test void malformedDniAndWeakPasswordAreRejected() throws Exception {
        var invalid=withCodeValue(lucia("lucia@example.test"),"000000");invalid.put("dni","123");
        postJson(ACCOUNTS,invalid,null).andExpect(status().isBadRequest());
        var weak=withCode(lucia("lucia@example.test"),"lucia@example.test");weak.put("password","abcdefgh");
        postJson(ACCOUNTS,weak,null).andExpect(status().isBadRequest());
    }
    @Test void malformedJsonAndInvalidLoginPayloadAreBadRequests() throws Exception {
        mvc.perform(post(ACCOUNTS+"/login").contentType("application/json").content("{")).andExpect(status().isBadRequest());
        postJson(ACCOUNTS+"/login",Map.of("email","not-an-email","password",PASSWORD),null)
                .andExpect(status().isBadRequest());
    }
    @Test void loginWorksWithEmailAndReturnsSafeOwnProfile() throws Exception {
        var token=patient();
        mvc.perform(get(ACCOUNTS+"/me").header("Authorization","Bearer "+token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("lucia@example.test")).andExpect(jsonPath("$.password").doesNotExist());
    }
    @Test void wrongPasswordAndUnknownUserReturnTheSameUnauthorizedError() throws Exception {
        register(lucia("lucia@example.test"));
        var a=body(postJson(ACCOUNTS+"/login",Map.of("email","lucia@example.test","password","Wrong1!Password"),null).andExpect(status().isUnauthorized()));
        var b=body(postJson(ACCOUNTS+"/login",Map.of("email","unknown@example.test","password",PASSWORD),null).andExpect(status().isUnauthorized()));
        assertThat(a).isEqualTo(b);
    }
    @Test void loginRequiresEmailAndPasswordAndDoesNotIssueATokenBeforeVerification() throws Exception {
        register(lucia("lucia@example.test"));
        postJson(ACCOUNTS+"/login",Map.of("password",PASSWORD),null).andExpect(status().isBadRequest());
        postJson(ACCOUNTS+"/login",Map.of("email","lucia@example.test"),null).andExpect(status().isBadRequest());
        var start=body(postJson(ACCOUNTS+"/login",Map.of("email","lucia@example.test","password",PASSWORD),null).andExpect(status().isOk()));
        assertThat(start).doesNotContain("accessToken");
        assertThat(text(start,"$.challengeId")).isNotBlank();
        assertThat(text(start,"$.maskedEmail")).endsWith("@example.test");
    }
    @Test void loginRejectsAnInvalidEmailCodeAndConsumesTheChallengeOnSuccess() throws Exception {
        register(lucia("lucia@example.test"));
        var start=body(postJson(ACCOUNTS+"/login",Map.of("email","lucia@example.test","password",PASSWORD),null).andExpect(status().isOk()));
        var challengeId=text(start,"$.challengeId");
        var code=emailCode("lucia@example.test","inicio de sesión");var wrong=code.equals("000000")?"111111":"000000";
        postJson(ACCOUNTS+"/login/verify",Map.of("challengeId",challengeId,"code",wrong),null).andExpect(status().isUnprocessableEntity());
        postJson(ACCOUNTS+"/login/verify",Map.of("challengeId",challengeId,"code",code),null).andExpect(status().isOk());
        postJson(ACCOUNTS+"/login/verify",Map.of("challengeId",challengeId,"code",code),null).andExpect(status().isUnprocessableEntity());
    }
    @Test void protectedRoutesRejectMissingAndTamperedBearerTokens() throws Exception {
        mvc.perform(get(ACCOUNTS+"/me")).andExpect(status().isUnauthorized());
        var token=patient();
        mvc.perform(get(ACCOUNTS+"/me").header("Authorization","Bearer "+token.substring(0,token.lastIndexOf('.')+1)+"invalid"))
                .andExpect(status().isUnauthorized());
    }
    @Test void expiredSessionIsRejectedAtItsDeadline() throws Exception {
        var token=patient();clock.advance(Duration.ofHours(1));
        mvc.perform(get(ACCOUNTS+"/me").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
    }
    @Test void logoutRevokesOnlyTheCurrentSession() throws Exception {
        var first=patient();var second=login("lucia@example.test",PASSWORD);
        postJson(ACCOUNTS+"/logout",Map.of(),first).andExpect(status().isNoContent());
        mvc.perform(get(ACCOUNTS+"/me").header("Authorization","Bearer "+first)).andExpect(status().isUnauthorized());
        mvc.perform(get(ACCOUNTS+"/me").header("Authorization","Bearer "+second)).andExpect(status().isOk());
    }
    @Test void inactiveAccountsCannotLoginOrContinueUsingExistingSessions() throws Exception {
        var token=patient();
        jdbc.update("UPDATE users SET is_active=false WHERE email=?","lucia@example.test");
        mvc.perform(get(ACCOUNTS+"/me").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
        postJson(ACCOUNTS+"/login",Map.of("email","lucia@example.test","password",PASSWORD,"role","PATIENT"),null).andExpect(status().isUnauthorized());
    }
    @Test void patientCannotReadOrUpdateAnotherAccountOrPatient() throws Exception {
        var token=patient();Long other=register(maria("maria@example.test"));
        Long otherPatient=facade.getUserById(other).flatMap(u->Optional.ofNullable(jdbc.queryForObject("SELECT id FROM patients WHERE id_user=?",Long.class,u.id()))).orElseThrow();
        mvc.perform(get(ACCOUNTS+"/"+other).header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/patients/"+otherPatient).header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
        mvc.perform(put(ACCOUNTS+"/"+other).header("Authorization","Bearer "+token).contentType("application/json")
                .content(json.writeValueAsString(Map.of("email","stolen@example.test","phone","987654321")))).andExpect(status().isForbidden());
    }
    @Test void ownContactUpdatePreservesIdentityRoleAndAuditCreationDate() throws Exception {
        var token=patient();var user=users.findByEmail("lucia@example.test").orElseThrow();
        var r=new HashMap<String,Object>(Map.of("email","new@example.test","phone","912345678"));r.put("role","SUPER_ADMIN");r.put("dni","30000001");
        mvc.perform(put(ACCOUNTS+"/"+user.getId()).header("Authorization","Bearer "+token).contentType("application/json").content(json.writeValueAsString(r)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("PATIENT")).andExpect(jsonPath("$.dni").value("71234821"));
        var updated=users.findById(user.getId()).orElseThrow();
        assertThat(updated.getCreatedAt()).isEqualTo(user.getCreatedAt());
        assertThat(outbox.findAll()).extracting(n->n.getRecipient()).contains("new@example.test","lucia@example.test");
    }
    @Test void anyPatientCanLinkAVerifiedMinor() throws Exception {
        register(maria("maria@example.test"));var token=login("maria@example.test",PASSWORD);
        postJson("/api/v1/patient-minors",minor(),token).andExpect(status().isCreated());
    }
    @Test void linkAndUnlinkPreserveMinorIdentityWhileRemovingAccessAndContactResolution() throws Exception {
        var token=patient();var user=users.findByEmail("lucia@example.test").orElseThrow();
        var r=body(postJson("/api/v1/patient-minors",minor(),token).andExpect(status().isCreated()));
        Long child=id(r,"$.patientId"),link=id(r,"$.id"),tutor=id(r,"$.tutorId");
        assertThat(facade.canManagePatient(user.getId(),child)).isTrue();
        assertThat(facade.getContactEmailForPatient(child)).contains("lucia@example.test");
        assertThat(facade.getPatientById(child).orElseThrow().userId()).isNull();
        mvc.perform(get("/api/v1/patients/"+tutor+"/minors").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(delete("/api/v1/patient-minors/"+link).header("Authorization","Bearer "+token)).andExpect(status().isNoContent());
        assertThat(facade.getPatientById(child)).isPresent();
        assertThat(facade.canManagePatient(user.getId(),child)).isFalse();
        assertThat(facade.getContactEmailForPatient(child)).isEmpty();
    }
    @Test void anExistingMinorLinkCannotBeTransferredOrDeletedByAnotherPatient() throws Exception {
        var token=patient();var linked=body(postJson("/api/v1/patient-minors",minor(),token).andExpect(status().isCreated()));
        register(maria("maria@example.test"));var other=login("maria@example.test",PASSWORD);
        postJson("/api/v1/patient-minors",minor(),other).andExpect(status().isConflict());
        mvc.perform(delete("/api/v1/patient-minors/"+id(linked,"$.id")).header("Authorization","Bearer "+other)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/patient-minors/"+id(linked,"$.id")).header("Authorization","Bearer "+other)).andExpect(status().isForbidden());
    }
    @Test void anAdultCannotBeLinkedAsAMinor() throws Exception {
        var token=patient();
        postJson("/api/v1/patient-minors",Map.of("dni","70000002","name","María","lastname","Vega","birthDate","1992-02-10","confirmFiliation",true),token)
                .andExpect(status().isUnprocessableEntity());
    }
    @Test void recoveryResponseDoesNotEnumerateAccountsAndSecretsAreNotStoredInPlaintext() throws Exception {
        patient();
        var known=body(postJson(ACCOUNTS+"/recover-password",Map.of("email","lucia@example.test"),null).andExpect(status().isAccepted()));
        var unknown=body(postJson(ACCOUNTS+"/recover-password",Map.of("email","unknown@example.test"),null).andExpect(status().isAccepted()));
        assertThat(known).isEqualTo(unknown);
        String raw=rawToken("lucia@example.test");
        assertThat(resetTokens.findAll()).hasSize(1).allSatisfy(t->assertThat(t.getTokenHash()).hasSize(64).doesNotContain(raw));
        assertThat(outbox.findAll()).allSatisfy(n->assertThat(n.getEncryptedBody()).doesNotContain(raw));
    }
    @Test void aRecoveryTokenIsSingleUseAndRevokesExistingSessions() throws Exception {
        var session=patient();var raw=recoveryToken("lucia@example.test");
        postJson(ACCOUNTS+"/reset-password",resetBody(raw),null).andExpect(status().isNoContent());
        postJson(ACCOUNTS+"/reset-password",resetBody(raw),null).andExpect(status().isUnprocessableEntity());
        mvc.perform(get(ACCOUNTS+"/me").header("Authorization","Bearer "+session)).andExpect(status().isUnauthorized());
        login("lucia@example.test","Updated1!Password");
    }
    @Test void recoveryLinkExpiresAfterExactlyFifteenMinutes() throws Exception {
        patient();var raw=recoveryToken("lucia@example.test");clock.advance(Duration.ofMinutes(15));
        postJson(ACCOUNTS+"/reset-password",resetBody(raw),null).andExpect(status().isUnprocessableEntity());
        login("lucia@example.test",PASSWORD);
    }
    @Test void aNewRecoveryRequestInvalidatesPreviousLinksAndMismatchedConfirmationCannotReset() throws Exception {
        patient();var first=recoveryToken("lucia@example.test");clock.advance(Duration.ofSeconds(1));var second=recoveryToken("lucia@example.test");
        postJson(ACCOUNTS+"/reset-password",resetBody(first),null).andExpect(status().isUnprocessableEntity());
        var invalid=new HashMap<>(resetBody(second));invalid.put("confirmPassword","Different1!Password");
        postJson(ACCOUNTS+"/reset-password",invalid,null).andExpect(status().isUnprocessableEntity());
        postJson(ACCOUNTS+"/reset-password",resetBody(second),null).andExpect(status().isNoContent());
    }
    @Test void concurrentRecoveryRedemptionAllowsExactlyOneWinner() throws Exception {
        patient();var raw=recoveryToken("lucia@example.test");
        try(var pool=Executors.newFixedThreadPool(2)) {
            var start=new CountDownLatch(1);
            Callable<Integer> redeem=()->{ start.await();return postJson(ACCOUNTS+"/reset-password",resetBody(raw),null).andReturn().getResponse().getStatus(); };
            var a=pool.submit(redeem);var b=pool.submit(redeem);start.countDown();
            assertThat(List.of(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS))).containsExactlyInAnyOrder(204,422);
        }
    }
    @Test void currentPasswordIsRequiredAndChangingItRevokesAllSessions() throws Exception {
        var token=patient();
        var oldLink=recoveryToken("lucia@example.test");
        postJson(ACCOUNTS+"/change-password",Map.of("currentPassword","Wrong1!Password","password","Updated1!Password","confirmPassword","Updated1!Password"),token)
                .andExpect(status().isUnauthorized());
        postJson(ACCOUNTS+"/change-password",Map.of("currentPassword",PASSWORD,"password","Updated1!Password","confirmPassword","Updated1!Password"),token)
                .andExpect(status().isNoContent());
        mvc.perform(get(ACCOUNTS+"/me").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
        login("lucia@example.test","Updated1!Password");
        postJson(ACCOUNTS+"/reset-password",resetBody(oldLink),null).andExpect(status().isUnprocessableEntity());
    }
    @Test void staffCreationRejectsAnUnverifiedIdentityAndAnExistingPatientDni() throws Exception {
        register(lucia("lucia@example.test"));var administrator=admin();
        var r=new HashMap<>(adult("87654321","Wrong","Ramos","1990-06-20","staff@example.test"));r.remove("password");
        postJson(ACCOUNTS+"/staff",r,administrator).andExpect(status().isUnprocessableEntity());
        r=new HashMap<>(lucia("another@example.test"));r.remove("password");
        postJson(ACCOUNTS+"/staff",r,administrator).andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM users",Integer.class)).isEqualTo(2);
    }
    @Test void staffAccountsRequireTheConfiguredCorporateEmailDomain() throws Exception {
        var administrator=admin();
        var r=new HashMap<>(adult("87654321","Diego","Ramos","1990-06-20","staff@personal.invalid"));r.remove("password");
        postJson(ACCOUNTS+"/staff",r,administrator).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("IAM_CORPORATE_EMAIL_REQUIRED"));
    }
    @Test void onlySuperAdminCreatesStaffAndTheInvitationSupportsTheStaffLoginRole() throws Exception {
        var token=patient();
        var r=new HashMap<>(adult("87654321","Diego","Ramos","1990-06-20","staff@example.test"));r.remove("password");r.put("role","SUPER_ADMIN");
        postJson(ACCOUNTS+"/staff",r,token).andExpect(status().isForbidden());
        var administrator=admin();
        postJson(ACCOUNTS+"/staff",r,administrator).andExpect(status().isCreated()).andExpect(jsonPath("$.role").value("ADMISSION_STAFF"));
        String raw=rawToken("staff@example.test");
        postJson(ACCOUNTS+"/reset-password",resetBody(raw),null).andExpect(status().isNoContent());
        var staffToken=login("staff@example.test","Updated1!Password");
        postJson(ACCOUNTS+"/staff",r,staffToken).andExpect(status().isForbidden());
        postJson("/api/v1/patient-minors",minor(),staffToken).andExpect(status().isForbidden());
        var user=users.findByEmail("staff@example.test").orElseThrow();
        mvc.perform(put(ACCOUNTS+"/"+user.getId()).header("Authorization","Bearer "+staffToken).contentType("application/json")
                .content(json.writeValueAsString(Map.of("email","staff-new@example.test","phone","912345678")))).andExpect(status().isOk());
    }
    @Test void assistedRecoveryRequiresAnAuditedSuperAdminInPersonApproval() throws Exception {
        var token=patient();
        postJson("/api/v1/account-recovery-requests",Map.of("dni","71234821","contactEmail","help@example.test"),null).andExpect(status().isAccepted());
        assertThat(users.findByEmail("help@example.test")).isEmpty();
        mvc.perform(get("/api/v1/account-recovery-requests").header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
        var administrator=admin();
        var list=body(mvc.perform(get("/api/v1/account-recovery-requests").header("Authorization","Bearer "+administrator)).andExpect(status().isOk()));
        var id=text(list,"$[0].id");
        postJson("/api/v1/account-recovery-requests/"+id+"/resolve",Map.of("identityCheckedInPerson",false),administrator).andExpect(status().isBadRequest());
        postJson("/api/v1/account-recovery-requests/"+id+"/resolve",Map.of("identityCheckedInPerson",true),administrator).andExpect(status().isNoContent());
        assertThat(users.findByEmail("help@example.test")).isPresent();
        assertThat(jdbc.queryForObject("SELECT status FROM account_recovery_requests WHERE id=?",String.class,id)).isEqualTo("RESOLVED");
        assertThat(jdbc.queryForObject("SELECT resolved_by FROM account_recovery_requests WHERE id=?",Long.class,id)).isNotNull();
        mvc.perform(get(ACCOUNTS+"/me").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
        postJson(ACCOUNTS+"/reset-password",resetBody(rawToken("help@example.test")),null).andExpect(status().isNoContent());
        login("help@example.test","Updated1!Password");
    }
    @Test void failedMailDeliveryKeepsTheNotificationForRetryAndSuccessRedactsTheLink() throws Exception {
        patient();recoveryToken("lucia@example.test");
        var n=outbox.findAll().stream().filter(e->e.getSubject().contains("Recupera")).findFirst().orElseThrow();
        doThrow(new MailSendException("Test unavailable SMTP")).when(mail).send(any(SimpleMailMessage.class));
        delivery.deliver(n.getId());
        assertThat(outbox.findById(n.getId()).orElseThrow().getStatus()).isEqualTo("PENDING");
        assertThat(outbox.findById(n.getId()).orElseThrow().getAttempts()).isEqualTo(1);
        reset(mail);clock.advance(Duration.ofMinutes(1));delivery.deliver(n.getId());
        var sent=outbox.findById(n.getId()).orElseThrow();
        assertThat(sent.getStatus()).isEqualTo("SENT");
        assertThat(cipher.decrypt(sent.getEncryptedBody())).isEqualTo("[Delivered]");
        verify(mail,times(1)).send(any(SimpleMailMessage.class));
        delivery.deliver(n.getId());verifyNoMoreInteractions(mail);
    }
    @Test void publicSwaggerDescribesIamEndpointsAndProtectedConfigurationRejectsPatients() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.paths['/api/v1/user-accounts/login']").exists());
        var token=patient();
        mvc.perform(get("/api/v1/config").header("Authorization","Bearer "+token)).andExpect(status().isForbidden());
    }
}
