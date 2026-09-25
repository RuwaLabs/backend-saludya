package com.ruwalabs.saludya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SaludyaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaludyaApplication.class, args);
    }

}
