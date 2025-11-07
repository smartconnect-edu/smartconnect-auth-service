package com.smartconnect.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Refresh token request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Refresh token request payload")
public class RefreshTokenRequest {

    @Schema(
        description = "Refresh token to generate new access token",
        example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huX2RvZSIsInR5cGUiOiJSRUZSRVNIIiwiaWF0IjoxNjI5ODc2NTQzLCJleHAiOjE2MzA0ODEzNDN9.dGhpc2lzYXJlZnJlc2h0b2tlbmV4YW1wbGU",
        required = true
    )
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}

