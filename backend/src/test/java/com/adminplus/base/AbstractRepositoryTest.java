package com.adminplus.base;

import com.adminplus.config.IntegrationTestConfig;
import com.adminplus.config.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

/**
 * Abstract base class for repository integration tests.
 * <p>
 * Provides:
 * <ul>
 *   <li>Spring Boot test context with PostgreSQL via Testcontainers</li>
 *   <li>TestEntityManager for database operations</li>
 *   <li>Security context setup for JPA auditing</li>
 * </ul>
 * <p>
 * Requires Docker with TCP endpoint enabled (Docker Desktop > Settings > General > "Expose daemon on tcp://localhost:2375").
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@SpringBootTest
@ActiveProfiles("test")
@Import({IntegrationTestConfig.class, TestcontainersConfiguration.class})
@Testcontainers
@Disabled("Integration tests require Docker with TCP endpoint enabled. Enable in Docker Desktop > Settings > General > 'Expose daemon on tcp://localhost:2375'")
public abstract class AbstractRepositoryTest {

    @Autowired
    protected EntityManager entityManager;

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
}