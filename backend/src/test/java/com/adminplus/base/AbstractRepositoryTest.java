package com.adminplus.base;

import com.adminplus.config.IntegrationTestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

/**
 * Abstract base class for repository integration tests.
 * <p>
 * Provides:
 * <ul>
 *   <li>Spring Boot test context with H2 in-memory database</li>
 *   <li>TestEntityManager for database operations</li>
 *   <li>Security context setup for JPA auditing</li>
 * </ul>
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
public abstract class AbstractRepositoryTest {

    @Autowired
    protected TestEntityManager entityManager;

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