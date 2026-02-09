package com.adminplus.security;

import com.adminplus.entity.RoleEntity;
import com.adminplus.entity.UserEntity;
import com.adminplus.entity.UserRoleEntity;
import com.adminplus.exception.BizException;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRepository;
import com.adminplus.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义用户详情服务
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        // 检查用户是否被删除
        if (user.getDeleted() != null && user.getDeleted()) {
            throw new BizException("用户已被删除");
        }

        // 查询用户的角色
        List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(user.getId());
        List<String> roleIds = userRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .toList();

        // 批量查询角色
        List<RoleEntity> roles = roleIds.isEmpty() ? List.of() : roleRepository.findAllById(roleIds);
        List<String> roleCodes = roles.stream()
                .filter(role -> role.getStatus() == 1)
                .map(RoleEntity::getCode)
                .collect(Collectors.toList());

        // 暂时不加载权限，权限在登录时通过 PermissionService 加载
        // 这里的 authorities 只包含角色，用于 Spring Security 的认证
        List<String> permissions = List.of();

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getStatus(),
                roleCodes,
                permissions
        );
    }
}