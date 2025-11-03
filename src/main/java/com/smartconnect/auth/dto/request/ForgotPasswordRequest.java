package com.smartconnect.auth.dto.request;

import com.smartconnect.auth.util.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Forgot password request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordRequest {

    @NotBlank(message = Constants.EMAIL_REQUIRED)
    @Email(message = Constants.EMAIL_INVALID)
    private String email;
}

