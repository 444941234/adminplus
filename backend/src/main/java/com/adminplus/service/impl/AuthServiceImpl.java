package com.adminplus.service.impl;

import com.adminplus.common.constant.SecurityConfigConstants;
import com.adminplus.common.exception.BizException;
import com.adminplus.common.properties.AppProperties;
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
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

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

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UserService userService;
    private final PermissionService permissionService;
    private final CaptchaService captchaService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;
    private final LogService logService;
    private final AppProperties appProperties;
    private final ConversionService conversionService;

    @Override
    @Transactional
    public LoginResponse login(UserLoginRequest request) {
        validateCaptcha(request.captchaId(), request.captchaCode(), request.username());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            UserEntity user = userService.getUserByUsername(request.username());
            List<String> roleCodes = userService.getUserRoleCodes(user.getId());

            String token = generateJwtToken(authentication, user, roleCodes);

            UserResponse userResponse = conversionService.convert(user, UserResponse.class);
            List<String> permissions = permissionService.getUserPermissions(user.getId());
            String refreshToken = refreshTokenService.createRefreshToken(user.getId());

            logService.log(LogEntry.operation("认证管理", OperationType.OTHER.getCode(),
                    "用户登录成功: " + LogMaskingUtils.maskUsername(request.username())));

            return new LoginResponse(token, refreshToken, SecurityConfigConstants.BEARER_PREFIX, userResponse, permissions);

        } catch (AuthenticationException e) {
            log.error("登录失败: username={}", LogMaskingUtils.maskUsername(request.username()));
            logService.log(LogEntry.operationBuilder("认证管理", OperationType.OTHER.getCode(),
                            "用户登录失败: " + LogMaskingUtils.maskUsername(request.username()))
                    .failed("用户名或密码错误")
                    .build());
            throw new BizException("用户名或密码错误");
        }
    }

    private void validateCaptcha(String captchaId, String captchaCode, String username) {
        if (!captchaService.validateCaptcha(captchaId, captchaCode)) {
            log.warn("验证码验证失败: username={}", LogMaskingUtils.maskUsername(username));
            throw new BizException("验证码错误或已过期，请重新输入");
        }
    }

    private String generateJwtToken(Authentication authentication, UserEntity user, List<String> roleCodes) {
        Instant now = Instant.now();
        int expirationHours = appProperties.getJwt().getExpirationHours();

        List<String> scopes = roleCodes.stream()
                .map(code -> code.startsWith(SecurityConfigConstants.ROLE_PREFIX) ? code.substring(SecurityConfigConstants.ROLE_PREFIX.length()) : code)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("adminplus")
                .issuedAt(now)
                .expiresAt(now.plus(expirationHours, ChronoUnit.HOURS))
                .subject(authentication.getName())
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .claim("deptId", user.getDeptId())
                .claim("scope", scopes.isEmpty() ? SecurityConfigConstants.DEFAULT_SCOPE : scopes)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
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
            logService.log(LogEntry.operation("认证管理", OperationType.OTHER.getCode(),
                    "用户退出: " + LogMaskingUtils.maskUsername(username)));

        } catch (Exception e) {
            log.error("登出时处理 Token 黑名单失败", e);
        }
    }

    private void blacklistCurrentToken(String userId) {
        HttpServletRequest request = WebUtils.getRequest();
        if (request == null) {
            blacklistAllUserTokens(userId);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(SecurityConfigConstants.BEARER_PREFIX)) {
            String token = authHeader.substring(SecurityConfigConstants.BEARER_PREFIX.length());
            tokenBlacklistService.blacklistToken(token, userId);
            log.info("用户登出，Token 已加入黑名单: userId={}", userId);
        } else {
            blacklistAllUserTokens(userId);
        }
    }

    private void blacklistAllUserTokens(String userId) {
        tokenBlacklistService.blacklistAllUserTokens(userId);
        log.info("用户登出，所有 Token 已加入黑名单: userId={}", userId);
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        return refreshTokenService.refreshAccessToken(refreshToken);
    }
}