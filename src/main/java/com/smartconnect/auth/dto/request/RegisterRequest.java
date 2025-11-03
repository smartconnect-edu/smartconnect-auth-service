package com.smartconnect.auth.dto.request;

import com.smartconnect.auth.model.enums.UserRole;
import com.smartconnect.auth.util.Constants;
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
public class RegisterRequest {

    @NotBlank(message = Constants.USERNAME_REQUIRED)
    @Size(min = 3, max = 50, message = Constants.USERNAME_MIN_LENGTH)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscore")
    private String username;

    @NotBlank(message = Constants.EMAIL_REQUIRED)
    @Email(message = Constants.EMAIL_INVALID)
    private String email;

    @NotBlank(message = Constants.PASSWORD_REQUIRED)
    @Size(min = 8, message = Constants.PASSWORD_MIN_LENGTH)
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
        message = "Password must contain at least one digit, one lowercase, one uppercase and one special character"
    )
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    private String phone;

    @NotNull(message = "Role is required")
    private UserRole role;
}

