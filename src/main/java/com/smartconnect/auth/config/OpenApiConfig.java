package com.smartconnect.auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger Configuration
 * Provides API documentation and interactive testing interface
 * 
 * Access Swagger UI at: http://localhost:3001/api/swagger-ui.html
 * Access API Docs at: http://localhost:3001/api/v3/api-docs
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
    servers = {
        @Server(
            url = "http://localhost:3001/api",
            description = "Local Development Server"
        ),
        @Server(
            url = "https://staging.smartconnect.com/api",
            description = "Staging Server"
        ),
        @Server(
            url = "https://api.smartconnect.com/api",
            description = "Production Server"
        )
    },
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
        Enter your JWT token in the format: **Bearer <token>**
        
        To get a token:
        1. Use the `/auth/register` endpoint to create an account
        2. Use the `/auth/login` endpoint to get your tokens
        3. Copy the `accessToken` from the response
        4. Click the **Authorize** button above
        5. Paste the token in the input field
        6. Click **Authorize** to set the token for all requests
        
        The token will be automatically included in all protected endpoint requests.
        """
)
public class OpenApiConfig {
    // Configuration is done through annotations
}
