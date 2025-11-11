package com.smartconnect.auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 * Provides API documentation and interactive testing interface
 * 
 * Access Swagger UI at: http://localhost:{server.port}{server.servlet.context-path}/swagger-ui.html
 * Access API Docs at: http://localhost:{server.port}{server.servlet.context-path}/v3/api-docs
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "SmartConnect Auth Service API",
        version = "1.0.0",
        description = """
            **Authentication & Authorization Service for SmartConnect Platform**
            
            This service provides:
            - User Registration & Login
            - JWT Token Management (Access & Refresh Tokens)
            - User Profile Management
            - Role-based Authorization (STUDENT, TEACHER, ADMIN)
            - Account Security (Auto-locking after failed attempts)
            - Token Blacklisting for Logout
            
            ## Authentication Flow:
            1. **Register**: POST /api/auth/register
            2. **Login**: POST /api/auth/login (returns access & refresh tokens)
            3. **Use Access Token**: Add `Authorization: Bearer <token>` header to protected endpoints
            4. **Refresh Token**: POST /api/auth/refresh when access token expires
            5. **Logout**: POST /api/auth/logout (blacklists the token)
            
            ## Security Features:
            - BCrypt password hashing
            - JWT token-based authentication
            - Redis-based token blacklisting
            - Account locking after 5 failed login attempts
            - Auto-unlock after 30 minutes
            - Token expiration (24 hours for access, 7 days for refresh)
            
            ## Available Roles:
            - **STUDENT**: Can access course content, submit assignments
            - **TEACHER**: Can create courses, grade assignments, manage students
            - **ADMIN**: Full system access, user management
            """,
        contact = @Contact(
            name = "SmartConnect Team",
            email = "support@smartconnect.com",
            url = "https://smartconnect.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    security = {
        @SecurityRequirement(name = "Bearer Authentication")
    }
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = """
        Paste ONLY your JWT access token (do NOT include the "Bearer " prefix).
        The Swagger UI will automatically add the "Bearer " prefix for you.
        
        To get a token:
        1. Use the `/api/v1/auth/register` endpoint to create an account
        2. Use the `/api/v1/auth/login` endpoint to get your tokens
        3. Copy the `accessToken` from the response
        4. Click the **Authorize** button above
        5. Paste the token (without "Bearer ") in the input field
        6. Click **Authorize** to set the token for all requests
        
        The token will be automatically included in all protected endpoint requests.
        """
)
public class OpenApiConfig {

    @Value("${server.port:3001}")
    private int serverPort;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        String baseUrl = "http://localhost:" + serverPort + contextPath;
        
        return new OpenAPI()
            .servers(List.of(
                new Server()
                    .url(baseUrl)
                    .description("Local Development Server"),
                new Server()
                    .url("https://staging.smartconnect.com/api")
                    .description("Staging Server"),
                new Server()
                    .url("https://api.smartconnect.com/api")
                    .description("Production Server")
            ));
    }
}
