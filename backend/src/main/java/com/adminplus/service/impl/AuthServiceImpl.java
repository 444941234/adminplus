package com.adminplus.service.impl;

import com.adminplus.common.constant.SecurityConfigConstants;
import com.adminplus.common.exception.BizException;
import com.adminplus.constants.HierarchyConstants;
import com.adminplus.common.security.AppUserDetails;
import com.adminplus.common.security.JwtTokenProvider;
import com.adminplus.enums.OperationType;
import com.adminplus.pojo.dto.request.UserLoginRequest;
import com.adminplus.pojo.dto.request.LogEntry;
import com.adminplus.pojo.dto.response.LoginResponse;
import com.adminplus.pojo.dto.response.UserResponse;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.service.*;
import com.adminplus.utils.LogMaskingUtils;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final PermissionService permissionService;
    private final CaptchaService captchaService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;
    private final LogService logService;
    private final ConversionService conversionService;

    @Override
    @Transactional
    public LoginResponse login(UserLoginRequest request) {
        validateCaptcha(request.captchaId(), request.captchaCode(), request.username());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
            UserEntity user = userService.getUserByUsername(userDetails.getUsername());

            String token = jwtTokenProvider.generateAccessToken(userDetails.getId());

            UserResponse userResponse = conversionService.convert(user, UserResponse.class);
            List<String> permissions = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
                    .filter(auth -> !auth.startsWith("ROLE_"))
                    .collect(Collectors.toList());
            String refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

            logService.log(LogEntry.login(LogMaskingUtils.maskUsername(request.username()), true, null));

            return new LoginResponse(token, refreshToken, SecurityConfigConstants.BEARER_PREFIX, userResponse, permissions);

        } catch (AuthenticationException e) {
            logService.log(LogEntry.login(LogMaskingUtils.maskUsername(request.username()), false, "用户名或密码错误"));
            throw new BizException("用户名或密码错误");
        }
    }

    private void validateCaptcha(String captchaId, String captchaCode, String username) {
        if (!captchaService.validateCaptcha(captchaId, captchaCode)) {
            log.warn("验证码验证失败: username={}", LogMaskingUtils.maskUsername(username));
            throw new BizException("验证码错误或已过期，请重新输入");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(String userId) {
        return userService.getUserById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserPermissions(String userId) {
        return permissionService.getUserPermissions(userId);
    }

    @Override
    public void logout() {
        try {
            String userId = SecurityUtils.getCurrentUserId();
            String username = SecurityUtils.getCurrentUsername();

            refreshTokenService.revokeAllUserTokens(userId);
            blacklistCurrentToken(userId);
            logService.log(LogEntry.operation(HierarchyConstants.MODULE_AUTH, OperationType.OTHER.getCode(),
                    "用户退出: " + LogMaskingUtils.maskUsername(username)));

        } catch (Exception e) {
            log.error("登出时处理 Token 黑名单失败", e);
        }
    }

    private void blacklistCurrentToken(String userId) {
        HttpServletRequest request = WebUtils.getRequest();
        if (request == null) {
            tokenBlacklistService.blacklistAllUserTokens(userId);
            return;
        }

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(SecurityConfigConstants.BEARER_PREFIX)) {
            String token = authHeader.substring(SecurityConfigConstants.BEARER_PREFIX.length());
            tokenBlacklistService.blacklistToken(token, userId);
            log.info("用户登出，Token 已加入黑名单: userId={}", userId);
        } else {
            tokenBlacklistService.blacklistAllUserTokens(userId);
        }
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        return refreshTokenService.refreshAccessToken(refreshToken);
    }
}