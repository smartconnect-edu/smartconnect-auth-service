package com.smartconnect.auth.dto.request;

import com.smartconnect.auth.util.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Login request payload")
public class LoginRequest {

    @Schema(
        description = "Username or email address",
        example = "john_doe",
        required = true
    )
    @NotBlank(message = "Username or email " + Constants.EMAIL_REQUIRED)
    private String username; // Can be username or email

    @Schema(
        description = "User password",
        example = "Password@123",
        required = true
    )
    @NotBlank(message = Constants.PASSWORD_REQUIRED)
    private String password;

    @Builder.Default
    @Schema(
        description = "Remember me for extended session",
        example = "true",
        defaultValue = "false"
    )
    private Boolean rememberMe = false;
}

