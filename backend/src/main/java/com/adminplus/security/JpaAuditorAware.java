package com.adminplus.security;

import com.adminplus.utils.SecurityUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        String currentUserId = SecurityUtils.getCurrentUserIdOrDefault();
        return Optional.of(currentUserId);
    }
}
