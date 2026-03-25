package com.adminplus.config;

import com.adminplus.service.CaptchaService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration test configuration
 * <p>
 * Provides test-specific beans for JWT encoding/decoding and JPA auditing.
 *
 * @author AdminPlus
 * @since 2026-03-25
 */
@TestConfiguration
public class IntegrationTestConfig {

    private static final RSAKey TEST_RSA_KEY;

    static {
        try {
            TEST_RSA_KEY = new RSAKeyGenerator(2048)
                    .keyID("test-key")
                    .generate();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate test RSA key", e);
        }
    }

    /**
     * Test RSA key for JWT signing/verification
     */
    @Bean
    @Primary
    public RSAKey testRsaKey() {
        return TEST_RSA_KEY;
    }

    /**
     * Test JWK source for JWT encoder
     */
    @Bean
    @Primary
    public JWKSource<SecurityContext> testJwkSource() {
        return new ImmutableJWKSet<>(new JWKSet(TEST_RSA_KEY));
    }

    /**
     * Test JWT encoder
     */
    @Bean
    @Primary
    public JwtEncoder testJwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * Test JWT decoder
     */
    @Bean
    @Primary
    public JwtDecoder testJwtDecoder() throws JOSEException {
        return NimbusJwtDecoder.withPublicKey(TEST_RSA_KEY.toRSAPublicKey()).build();
    }

    /**
     * Test auditor provider for JPA auditing
     */
    @Bean
    @Primary
    public AuditorAware<String> testAuditorProvider() {
        return () -> Optional.of("test-user");
    }

    /**
     * Mock captcha service that always validates successfully
     */
    @Bean
    @Primary
    public CaptchaService testCaptchaService() {
        CaptchaService captchaService = mock(CaptchaService.class);
        when(captchaService.validateCaptcha(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(true);
        return captchaService;
    }
}