package com.smartconnect.auth.dto.request;

import com.smartconnect.auth.model.enums.UserRole;
import com.smartconnect.auth.util.Constants;
import com.smartconnect.auth.validation.OptionalPhone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Registration request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User registration request payload")
public class RegisterRequest {

    @Schema(
        description = "Username (3-50 characters, letters, numbers and underscore only)",
        example = "john_doe",
        required = true
    )
    @NotBlank(message = Constants.USERNAME_REQUIRED)
    @Size(min = 3, max = 50, message = Constants.USERNAME_MIN_LENGTH)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscore")
    private String username;

    @Schema(
        description = "Email address",
        example = "john.doe@example.com",
        required = true
    )
    @NotBlank(message = Constants.EMAIL_REQUIRED)
    @Email(message = Constants.EMAIL_INVALID)
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "Email must be a valid format"
    )
    private String email;

    @Schema(
        description = "Password (min 8 characters, must contain digit, lowercase, uppercase and special character)",
        example = "Password@123",
        required = true
    )
    @NotBlank(message = Constants.PASSWORD_REQUIRED)
    @Size(min = 8, message = Constants.PASSWORD_MIN_LENGTH)
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
        message = "Password must contain at least one digit, one lowercase, one uppercase and one special character"
    )
    private String password;

    @Schema(
        description = "Full name of the user",
        example = "John Doe",
        required = true
    )
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Schema(
        description = "Phone number (10-15 digits, optional)",
        example = "0912345678"
    )
    @OptionalPhone
    private String phone;

    @Schema(
        description = "User role",
        example = "STUDENT",
        required = true,
        allowableValues = {"STUDENT", "TEACHER", "ADMIN", "SUPER_ADMIN"}
    )
    @NotNull(message = "Role is required")
    private UserRole role;
}

