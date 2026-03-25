package com.adminplus.util;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Test utilities for JWT token generation and authentication.
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
public final class TestJwtUtils {

    private TestJwtUtils() {
        // Utility class
    }

    /**
     * Generate a JWT token for testing
     *
     * @param jwtEncoder  JWT encoder
     * @param userId      User ID
     * @param username    Username
     * @param authorities Authorities/roles
     * @return JWT token string
     */
    public static String generateToken(JwtEncoder jwtEncoder, String userId, String username, String... authorities) {
        return generateToken(jwtEncoder, userId, username, null, authorities);
    }

    /**
     * Generate a JWT token for testing with department ID
     *
     * @param jwtEncoder  JWT encoder
     * @param userId      User ID
     * @param username    Username
     * @param deptId      Department ID
     * @param authorities Authorities/roles
     * @return JWT token string
     */
    public static String generateToken(JwtEncoder jwtEncoder, String userId, String username, String deptId, String... authorities) {
        Instant now = Instant.now();

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer("adminplus")
                .issuedAt(now)
                .expiresAt(now.plus(2, ChronoUnit.HOURS))
                .subject(username)
                .claim("userId", userId)
                .claim("scope", String.join(" ", authorities));

        if (deptId != null) {
            claimsBuilder.claim("deptId", deptId);
        }

        JwtClaimsSet claims = claimsBuilder.build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * Add JWT authorization header to a request
     *
     * @param builder Request builder
     * @param token   JWT token
     * @return Request builder with authorization header
     */
    public static MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder builder, String token) {
        return builder.header("Authorization", "Bearer " + token);
    }

    /**
     * Create authenticated request with generated token
     *
     * @param builder     Request builder
     * @param jwtEncoder  JWT encoder
     * @param userId      User ID
     * @param username    Username
     * @param authorities Authorities
     * @return Request builder with authorization header
     */
    public static MockHttpServletRequestBuilder withAuth(
            MockHttpServletRequestBuilder builder,
            JwtEncoder jwtEncoder,
            String userId,
            String username,
            String... authorities) {
        return withAuth(builder, generateToken(jwtEncoder, userId, username, authorities));
    }
}