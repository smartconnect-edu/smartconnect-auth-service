package com.smartconnect.auth.dto.request;

import com.smartconnect.auth.util.Constants;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Username or email " + Constants.EMAIL_REQUIRED)
    private String identifier; // username or email

    @NotBlank(message = Constants.PASSWORD_REQUIRED)
    private String password;

    private Boolean rememberMe = false;
}

