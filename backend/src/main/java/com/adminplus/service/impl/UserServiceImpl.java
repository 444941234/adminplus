package com.adminplus.service.impl;

import com.adminplus.dto.UserCreateReq;
import com.adminplus.dto.UserUpdateReq;
import com.adminplus.entity.RoleEntity;
import com.adminplus.entity.UserEntity;
import com.adminplus.entity.UserRoleEntity;
import com.adminplus.exception.BizException;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.UserService;
import com.adminplus.utils.XssUtils;
import com.adminplus.vo.UserVO;
import com.adminplus.vo.PageResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    @Override
    @Transactional(readOnly = true)
    public PageResultVO<UserVO> getUserList(Integer page, Integer size, String keyword) {
        var pageable = PageRequest.of(page - 1, size);
        var pageResult = userRepository.findAll(pageable);

        // 批量查询所有用户角色
        List<Long> userIds = pageResult.getContent().stream()
                .map(UserEntity::getId)
                .toList();
        List<UserRoleEntity> allUserRoles = userIds.isEmpty()
                ? List.of()
                : userRoleRepository.findByUserIdIn(userIds);

        // 批量查询所有角色
        List<Long> roleIds = allUserRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .distinct()
                .toList();
        List<RoleEntity> allRoles = roleIds.isEmpty()
                ? List.of()
                : roleRepository.findAllById(roleIds);

        // 构建角色映射
        Map<Long, String> roleMap = allRoles.stream()
                .collect(Collectors.toMap(RoleEntity::getId, RoleEntity::getName));

        // 构建用户角色映射
        Map<Long, List<String>> userRoleMap = new HashMap<>();
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
    public UserVO getUserById(Long id) {
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

        var user = new UserEntity();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setNickname(XssUtils.escape(req.nickname()));
        user.setEmail(XssUtils.escape(req.email()));
        user.setPhone(XssUtils.escape(req.phone()));
        user.setAvatar(req.avatar());
        user.setStatus(1);

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
    public UserVO updateUser(Long id, UserUpdateReq req) {
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
    public void deleteUser(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new BizException("用户不存在"));

        user.setDeleted(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUserStatus(Long id, Integer status) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new BizException("用户不存在"));

        user.setStatus(status);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new BizException("用户不存在"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 检查用户是否存在
        if (!userRepository.existsById(userId)) {
            throw new BizException("用户不存在");
        }

        // 验证所有角色是否存在
        for (Long roleId : roleIds) {
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
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getUserRoleIds(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new BizException("用户不存在");
        }
        return userRoleRepository.findByUserId(userId).stream()
                .map(UserRoleEntity::getRoleId)
                .toList();
    }
}