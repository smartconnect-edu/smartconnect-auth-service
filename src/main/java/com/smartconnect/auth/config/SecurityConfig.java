package com.smartconnect.auth.config;

import com.smartconnect.auth.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration
 * Configures Spring Security with JWT authentication and public endpoints
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Configure HTTP Security
     * - Disable CSRF (using JWT tokens)
     * - Configure public endpoints (Swagger, Health checks, Auth endpoints)
     * - Set session to STATELESS
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF since we're using JWT
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - accessible without authentication
                .requestMatchers(Constants.PUBLIC_URLS).permitAll()
                
                // Swagger UI and OpenAPI endpoints
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                
                // Actuator endpoints
                .requestMatchers(
                    "/actuator/**",
                    "/api/actuator/**"
                ).permitAll()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Stateless session management (JWT-based)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    /**
     * Password encoder bean
     * Uses BCrypt for password hashing
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

