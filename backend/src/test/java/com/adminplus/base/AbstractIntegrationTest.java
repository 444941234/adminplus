package com.adminplus.base;

import com.adminplus.config.IntegrationTestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Abstract base class for integration tests.
 * <p>
 * Provides:
 * <ul>
 *   <li>Spring Boot test context with random port</li>
 *   <li>MockMvc for HTTP request testing</li>
 *   <li>JWT token generation utilities</li>
 *   <li>Security context setup for JPA auditing</li>
 * </ul>
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JwtEncoder jwtEncoder;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Set up security context for JPA auditing
     */
    @BeforeEach
    void setUpSecurityContext() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "test-user",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * Clear security context after each test
     */
    @AfterEach
    void tearDownSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Generate a JWT token for testing
     *
     * @param userId      User ID
     * @param username    Username
     * @param authorities Authorities/roles (e.g., "user:view", "role:create")
     * @return JWT token string
     */
    protected String generateToken(String userId, String username, String[] authorities) {
        return generateToken(userId, username, null, authorities);
    }

    /**
     * Generate a JWT token for testing with department ID
     *
     * @param userId      User ID
     * @param username    Username
     * @param deptId      Department ID (can be null)
     * @param authorities Authorities/roles
     * @return JWT token string
     */
    protected String generateToken(String userId, String username, String deptId, String[] authorities) {
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
    protected MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder builder, String token) {
        return builder.header("Authorization", "Bearer " + token);
    }

    /**
     * Create authenticated request with generated token
     *
     * @param builder     Request builder
     * @param userId      User ID
     * @param username    Username
     * @param authorities Authorities
     * @return Request builder with authorization header
     */
    protected MockHttpServletRequestBuilder withAuth(
            MockHttpServletRequestBuilder builder,
            String userId,
            String username,
            String... authorities) {
        return withAuth(builder, generateToken(userId, username, authorities));
    }
}