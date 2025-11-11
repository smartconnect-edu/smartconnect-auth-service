package com.smartconnect.auth.service;

import com.smartconnect.auth.model.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

/**
 * JWT Service interface
 */
public interface JwtService {

	String CLAIM_USER_ID = "userId";
	String CLAIM_EMAIL = "email";
	String CLAIM_ROLE = "role";
	String CLAIM_TOKEN_TYPE = "tokenType";
	String CLAIM_NONCE = "nonce";

	String TOKEN_TYPE_REFRESH = "REFRESH";

	String generateAccessToken(User user);

	String generateRefreshToken(User user);

	String getUsernameFromToken(String token);

	UUID getUserIdFromToken(String token);

	String getRoleFromToken(String token);

	Date getExpirationDateFromToken(String token);

	<T> T extractClaim(String token, Function<io.jsonwebtoken.Claims, T> claimsResolver);

	boolean validateToken(String token, UserDetails userDetails);

	long getAccessTokenExpiration();

	long getRefreshTokenExpiration();
}