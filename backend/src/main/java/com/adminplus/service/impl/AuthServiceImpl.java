package com.adminplus.service.impl;

import com.adminplus.dto.UserLoginReq;
import com.adminplus.entity.RoleEntity;
import com.adminplus.entity.UserEntity;
import com.adminplus.entity.UserRoleEntity;
import com.adminplus.exception.BizException;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.constants.LogStatus;
import com.adminplus.constants.OperationType;
import com.adminplus.service.AuthService;
import com.adminplus.service.CaptchaService;
import com.adminplus.service.LogService;
import com.adminplus.service.PermissionService;
import com.adminplus.service.RefreshTokenService;
import com.adminplus.service.TokenBlacklistService;
import com.adminplus.service.UserService;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.vo.LoginResp;
import com.adminplus.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UserService userService;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PermissionService permissionService;
    private final CaptchaService captchaService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;
    private final LogService logService;
    private final StringRedisTemplate redisTemplate;

    @Override
    public LoginResp login(UserLoginReq req) {
        // 验证验证码
        if (!captchaService.validateCaptcha(req.captchaId(), req.captchaCode())) {
            log.warn("验证码验证失败: username={}", maskUsername(req.username()));
            // 优化验证码错误提示，区分不同错误类型
            String redisKey = "captcha:" + req.captchaId();
            String storedCode = redisTemplate.opsForValue().get(redisKey);
            if (storedCode == null) {
                throw new BizException("验证码已过期，请重新获取");
            } else {
                throw new BizException("验证码错误，请重新输入");
            }
        }

        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );

            // 获取用户信息
            UserEntity user = userService.getUserByUsername(req.username());

            // 生成 JWT Token（过期时间改为 2 小时）
            Instant now = Instant.now();
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("adminplus")
                    .issuedAt(now)
                    .expiresAt(now.plus(2, ChronoUnit.HOURS))  // 从 24 小时改为 2 小时
                    .subject(authentication.getName())
                    .claim("userId", user.getId())
                    .claim("username", user.getUsername())
                    .claim("scope", "ROLE_USER")
                    .build();

            String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

            // 查询用户角色
            List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(user.getId());
            List<String> roleNames = userRoles.stream()
                    .map(UserRoleEntity::getRoleId)
                    .map(roleId -> roleRepository.findById(roleId).orElse(null))
                    .filter(Objects::nonNull)
                    .map(RoleEntity::getName)
                    .collect(Collectors.toList());

            UserVO userVO = new UserVO(
                    user.getId(),
                    user.getUsername(),
                    user.getNickname(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAvatar(),
                    user.getStatus(),
                    roleNames,
                    user.getCreateTime(),
                    user.getUpdateTime()
            );

            // 查询用户权限
            List<String> permissions = permissionService.getUserPermissions(user.getId());

            // 生成 Refresh Token
            String refreshToken = refreshTokenService.createRefreshToken(user.getId());

            // 记录登录审计日志
            logService.log("认证管理", OperationType.OTHER, "用户登录成功: " + maskUsername(req.username()));

            return new LoginResp(token, refreshToken, "Bearer", userVO, permissions);

        } catch (AuthenticationException e) {
            log.error("登录失败: username={}", maskUsername(req.username()));

            // 记录登录失败审计日志
            logService.log("认证管理", OperationType.OTHER, "用户登录失败: " + maskUsername(req.username()),
                    LogStatus.FAILED, "用户名或密码错误");

            throw new BizException("用户名或密码错误");
        }
    }

    @Override
    public UserVO getCurrentUser(String username) {
        UserEntity user = userService.getUserByUsername(username);

        // 查询用户角色
        List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(user.getId());
        List<String> roleNames = userRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .map(roleId -> roleRepository.findById(roleId).orElse(null))
                .filter(Objects::nonNull)
                .map(RoleEntity::getName)
                .collect(Collectors.toList());

        return new UserVO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getStatus(),
                roleNames,
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }

    @Override
    public List<String> getCurrentUserPermissions(String username) {
        UserEntity user = userService.getUserByUsername(username);
        return permissionService.getUserPermissions(user.getId());
    }

    @Override
    public void logout() {
        try {
            // 获取当前用户ID
            String userId = SecurityUtils.getCurrentUserId();
            String username = SecurityUtils.getCurrentUsername();

            // 撤销用户的所有 Refresh Token
            refreshTokenService.revokeAllUserTokens(userId);

            // 获取当前请求
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");

                // 将 Token 加入黑名单
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    tokenBlacklistService.blacklistToken(token, userId);
                    log.info("用户登出，Token 已加入黑名单: userId={}", userId);
                } else {
                    // 如果没有 Token，将用户的所有 Token 加入黑名单
                    tokenBlacklistService.blacklistAllUserTokens(userId);
                    log.info("用户登出，所有 Token 已加入黑名单: userId={}", userId);
                }
            } else {
                // 如果无法获取请求，将用户的所有 Token 加入黑名单
                tokenBlacklistService.blacklistAllUserTokens(userId);
                log.info("用户登出，所有 Token 已加入黑名单: userId={}", userId);
            }

            // 记录登出审计日志
            logService.log("认证管理", OperationType.OTHER, "用户��出: " + maskUsername(username));

        } catch (Exception e) {
            log.error("登出时处理 Token 黑名单失败", e);
            // 即使失败也不影响登出流程
        }
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        return refreshTokenService.refreshAccessToken(refreshToken);
    }

    /**
     * 隐藏用户名敏感信息
     */
    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) {
            return "***";
        }
        return username.charAt(0) + "***" + username.charAt(username.length() - 1);
    }
}