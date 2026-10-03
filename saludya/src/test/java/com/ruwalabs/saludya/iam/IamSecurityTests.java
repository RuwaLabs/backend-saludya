package com.ruwalabs.saludya.iam;

import com.ruwalabs.saludya.iam.infrastructure.authorization.AuthRateLimitFilter;
import com.ruwalabs.saludya.iam.infrastructure.identity.ReniecService;
import com.ruwalabs.saludya.iam.infrastructure.identity.UnavailableIdentityGateway;
import com.ruwalabs.saludya.iam.infrastructure.notifications.NotificationCipher;
import com.ruwalabs.saludya.iam.infrastructure.hashing.BCryptHashingService;
import com.ruwalabs.saludya.iam.application.internal.IdentityVerificationService;
import com.ruwalabs.saludya.iam.domain.model.valueobjects.Dni;
import com.ruwalabs.saludya.iam.domain.model.exceptions.IamException;
import com.ruwalabs.saludya.iam.domain.services.IdentityGateway;
import com.sun.net.httpserver.HttpServer;
import org.springframework.mock.web.*;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class IamSecurityTests {
    @Test void encryptedNotificationsUseRandomNoncesAndAuthenticateTheirContents() {
        var cipher=new NotificationCipher("c2FsdWR5YS1kZXYta2V5LW5vdC1mb3ItcHJvZHVjdGk=");
        String first=cipher.encrypt("a private recovery link"),second=cipher.encrypt("a private recovery link");
        assertThat(first).isNotEqualTo(second);
        assertThat(cipher.decrypt(first)).isEqualTo("a private recovery link");
        byte[] tampered=Base64.getDecoder().decode(first);tampered[tampered.length-1]^=1;
        assertThatThrownBy(()->cipher.decrypt(Base64.getEncoder().encodeToString(tampered))).isInstanceOf(IllegalStateException.class);
    }
    @Test void bcryptRejectsPasswordsExceedingItsUtf8ByteLimit() {
        var hashing=new BCryptHashingService();
        assertThatThrownBy(()->hashing.hash("é".repeat(40)+"A1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->hashing.hash("a".repeat(73)+"1")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void perIpLimiterRejectsExcessSensitiveRequestsAndAllowsThemAfterTheWindow() throws Exception {
        var clock=new IamIntegrationTests.MutableClock();var filter=new AuthRateLimitFilter(clock,2,true);
        for(int i=0;i<3;i++) {
            var request=new MockHttpServletRequest("POST","/api/v1/user-accounts/login");request.setRemoteAddr("127.0.0.1");
            var response=new MockHttpServletResponse();
            filter.doFilter(request,response,(req,res)->((jakarta.servlet.http.HttpServletResponse)res).setStatus(204));
            assertThat(response.getStatus()).isEqualTo(i<2?204:429);
            if(i==2) assertThat(response.getHeader("Retry-After")).isEqualTo("60");
        }
        clock.advance(Duration.ofMinutes(1));
        var response=new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("POST","/api/v1/user-accounts/login"),response,(req,res)->((jakarta.servlet.http.HttpServletResponse)res).setStatus(204));
        assertThat(response.getStatus()).isEqualTo(204);
    }
    @Test void adulthoodUsesVerifiedBirthDateAndTheLimaCalendarBoundary() {
        var clock=Clock.fixed(Instant.parse("2026-10-03T03:00:00Z"),ZoneOffset.UTC);
        var identity=new IdentityGateway.OfficialIdentity(new Dni("71234821"),"Lucía","Torres",LocalDate.parse("2008-10-03"),Set.of());
        var service=new IdentityVerificationService(dni->identity,clock);
        assertThat(service.today()).isEqualTo(LocalDate.parse("2026-10-02"));
        assertThatThrownBy(()->service.requireAdult(identity)).isInstanceOf(IamException.class);
        var nextDay=new IdentityVerificationService(dni->identity,Clock.fixed(Instant.parse("2026-10-03T05:00:00Z"),ZoneOffset.UTC));
        assertThatCode(()->nextDay.requireAdult(identity)).doesNotThrowAnyException();
    }
    @Test void trustedNamesCanBeAccentNormalizedButBirthDateCannotBeAltered() {
        var identity=new IdentityGateway.OfficialIdentity(new Dni("71234821"),"Lucía","Torres",LocalDate.parse("1995-04-15"),Set.of());
        var service=new IdentityVerificationService(dni->identity,Clock.systemUTC());
        assertThatCode(()->service.verify("71234821","  LUCIA  ","torres",identity.birthDate())).doesNotThrowAnyException();
        assertThatThrownBy(()->service.verify("71234821","Lucía","Torres",LocalDate.parse("1990-01-01"))).isInstanceOf(IamException.class);
    }
    @Test void anUnconfiguredIdentityGatewayFailsClosed() {
        assertThatThrownBy(()->new UnavailableIdentityGateway().lookup(new Dni("71234821")))
                .isInstanceOfSatisfying(IamException.class,e->assertThat(e.getStatus()).isEqualTo(503));
    }
    @Test void identityProviderConfigurationRejectsInsecureRemoteUrlsAndFakeLocalhostHosts() {
        assertThatThrownBy(()->new ReniecService("http://identity.example.test/{dni}","")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->new ReniecService("http://localhost.attacker.test/{dni}","")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void trustedIdentityGatewayHandlesValidMissingAndUnavailableResponses() throws Exception {
        var server=HttpServer.create(new InetSocketAddress("localhost",0),0);
        server.createContext("/identity",exchange->{
            String dni=exchange.getRequestURI().getPath().substring("/identity/".length());
            int status=dni.equals("71234821")?200:dni.equals("00000000")?404:500;
            String response=status==200?"{\"dni\":\"71234821\",\"name\":\"Lucía\",\"lastname\":\"Torres\",\"birthDate\":\"1995-04-15\",\"guardianDnis\":[]}":"{}";
            byte[] bytes=response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type","application/json");
            exchange.sendResponseHeaders(status,bytes.length);exchange.getResponseBody().write(bytes);exchange.close();
        });
        server.start();
        try {
            var service=new ReniecService("http://localhost:"+server.getAddress().getPort()+"/identity/{dni}","test-provider-key");
            assertThat(service.lookup(new Dni("71234821")).birthDate()).isEqualTo(LocalDate.parse("1995-04-15"));
            assertThatThrownBy(()->service.lookup(new Dni("00000000"))).isInstanceOfSatisfying(IamException.class,e->assertThat(e.getStatus()).isEqualTo(422));
            assertThatThrownBy(()->service.lookup(new Dni("99999999"))).isInstanceOfSatisfying(IamException.class,e->assertThat(e.getStatus()).isEqualTo(503));
        } finally { server.stop(0); }
    }
    @Test void incompleteIdentityProviderResponsesCannotApproveRegistration() throws Exception {
        var server=HttpServer.create(new InetSocketAddress("localhost",0),0);
        server.createContext("/",exchange->{
            byte[] bytes="{\"dni\":\"71234821\",\"name\":\"Lucía\",\"lastname\":\"Torres\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(200,bytes.length);
            exchange.getResponseBody().write(bytes);exchange.close();
        });
        server.start();
        try {
            var service=new ReniecService("http://localhost:"+server.getAddress().getPort()+"/{dni}","");
            assertThatThrownBy(()->service.lookup(new Dni("71234821"))).isInstanceOfSatisfying(IamException.class,e->assertThat(e.getStatus()).isEqualTo(503));
        } finally { server.stop(0); }
    }
}
