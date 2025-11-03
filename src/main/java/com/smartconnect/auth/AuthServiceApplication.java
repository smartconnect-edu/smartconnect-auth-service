package com.smartconnect.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * SmartConnect Auth Service - Authentication & Authorization Microservice
 * 
 * @author SmartConnect Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

