package com.adminplus.service.impl;

import com.adminplus.constants.OperationType;
import com.adminplus.dto.UserCreateReq;
import com.adminplus.dto.UserUpdateReq;
import com.adminplus.entity.RoleEntity;
import com.adminplus.entity.UserEntity;
import com.adminplus.entity.UserRoleEntity;
import com.adminplus.exception.BizException;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.LogService;
import com.adminplus.service.UserService;
import com.adminplus.utils.PasswordUtils;
import com.adminplus.utils.XssUtils;
import com.adminplus.vo.UserVO;
import com.adminplus.vo.PageResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogService logService;

    @Override
    @Transactional(readOnly = true)
    public PageResultVO<UserVO> getUserList(Integer page, Integer size, String keyword) {
        var pageable = PageRequest.of(page - 1, size);
        var pageResult = userRepository.findAll(pageable);

        // 批量查询所有用户角色
        List<String> userIds = pageResult.getContent().stream()
                .map(UserEntity::getId)
                .toList();
        List<UserRoleEntity> allUserRoles = userIds.isEmpty()
                ? List.of()
                : userRoleRepository.findByUserIdIn(userIds);

        // 批量查询所有角色
        List<String> roleIds = allUserRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .distinct()
                .toList();
        List<RoleEntity> allRoles = roleIds.isEmpty()
                ? List.of()
                : roleRepository.findAllById(roleIds);

        // 构建角色映射
        Map<String, String> roleMap = allRoles.stream()
                .collect(Collectors.toMap(RoleEntity::getId, RoleEntity::getName));

        // 构建用户角色映射
        Map<String, List<String>> userRoleMap = new HashMap<>();
        for (UserRoleEntity ur : allUserRoles) {
            String roleName = roleMap.get(ur.getRoleId());
            if (roleName != null) {
                userRoleMap.computeIfAbsent(ur.getUserId(), k -> new ArrayList<>()).add(roleName);
            }
        }

        var records = pageResult.getContent().stream().map(user -> {
            List<String> roleNames = userRoleMap.getOrDefault(user.getId(), List.of());

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
        }).toList();

        return new PageResultVO<>(
                records,
                pageResult.getTotalElements(),
                pageResult.getNumber() + 1,
                pageResult.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserVO getUserById(String id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new BizException("用户不存在"));

        List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(id);
        List<String> roleNames = userRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .map(roleId -> roleRepository.findById(roleId).orElse(null))
                .filter(role -> role != null)
                .map(RoleEntity::getName)
                .toList();

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
    @Async
    public CompletableFuture<PageResultVO<UserVO>> getUserListAsync(Integer page, Integer size, String keyword) {
        log.info("使用虚拟线程异步查询用户列表");
        return CompletableFuture.completedFuture(getUserList(page, size, keyword));
    }

    @Override
    @Async
    public CompletableFuture<UserVO> getUserByIdAsync(String id) {
        log.info("使用虚拟线程异步查询用户: {}", id);
        return CompletableFuture.completedFuture(getUserById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BizException("用户不存在"));
    }

    @Override
    @Transactional
    public UserVO createUser(UserCreateReq req) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(req.username())) {
            throw new BizException("用户名已存在");
        }

        // 验证密码强度
        if (!PasswordUtils.isStrongPassword(req.password())) {
            throw new BizException(PasswordUtils.getPasswordStrengthHint(req.password()));
        }

        var user = new UserEntity();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setNickname(XssUtils.escape(req.nickname()));
        user.setEmail(XssUtils.escape(req.email()));
        user.setPhone(XssUtils.escape(req.phone()));
        user.setAvatar(req.avatar());
        user.setStatus(1);

        user = userRepository.save(user);

        // 记录审计日志
        logService.log("用户管理", OperationType.CREATE, "创建用户: " + user.getUsername());

        return new UserVO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getStatus(),
                List.of(),
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }

    @Override
    @Transactional
    public UserVO updateUser(String id, UserUpdateReq req) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new BizException("用户不存在"));

        if (req.nickname() != null) {
            user.setNickname(XssUtils.escape(req.nickname()));
        }
        if (req.email() != null) {
            user.setEmail(XssUtils.escape(req.email()));
        }
        if (req.phone() != null) {
            user.setPhone(XssUtils.escape(req.phone()));
        }
        if (req.avatar() != null) {
            user.setAvatar(req.avatar());
        }
        if (req.status() != null) {
            user.setStatus(req.status());
        }

        user = userRepository.save(user);

        return new UserVO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getStatus(),
                List.of(),
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new BizException("用户不存在"));

        user.setDeleted(true);
        userRepository.save(user);

        // 记录审计日志
        logService.log("用户管理", OperationType.DELETE, "删除用户: " + user.getUsername());
    }

    @Override
    @Transactional
    public void updateUserStatus(String id, Integer status) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new BizException("用户不存在"));

        user.setStatus(status);
        userRepository.save(user);

        // 记录审计日志
        logService.log("用户管理", OperationType.UPDATE, "更新用户状态: " + user.getUsername() + " -> " + status);
    }

    @Override
    @Transactional
    public void resetPassword(String id, String newPassword) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new BizException("用户不存在"));

        // 验证新密码强度（确保所有密码修改操作都使用相同的密码强度规则）
        if (!PasswordUtils.isStrongPassword(newPassword)) {
            throw new BizException(PasswordUtils.getPasswordStrengthHint(newPassword));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 记录审计日志（使用掩码隐藏用户名）
        logService.log("用户管理", OperationType.UPDATE, "重置密码: " + maskUsername(user.getUsername()));
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

    @Override
    @Transactional
    public void assignRoles(String userId, List<String> roleIds) {
        // 检查用户是否存在
        if (!userRepository.existsById(userId)) {
            throw new BizException("用户不存在");
        }

        // 验证所有角色是否存在
        for (String roleId : roleIds) {
            if (!roleRepository.existsById(roleId)) {
                throw new BizException("角色不存在，ID: " + roleId);
            }
        }

        // 删除原有的用户-角色关联
        userRoleRepository.deleteByUserId(userId);

        // 添加新的用户-角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            List<UserRoleEntity> userRoles = roleIds.stream().map(roleId -> {
                var userRole = new UserRoleEntity();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                return userRole;
            }).toList();
            userRoleRepository.saveAll(userRoles);
        }

        // 记录审计日志
        var user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            String roleNames = roleIds.stream()
                    .map(roleId -> roleRepository.findById(roleId).orElse(null))
                    .filter(Objects::nonNull)
                    .map(RoleEntity::getName)
                    .collect(Collectors.joining(", "));
            logService.log("用户管理", OperationType.UPDATE, "分配角色: " + user.getUsername() + " -> " + roleNames);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserRoleIds(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new BizException("用户不存在");
        }
        return userRoleRepository.findByUserId(userId).stream()
                .map(UserRoleEntity::getRoleId)
                .toList();
    }
}