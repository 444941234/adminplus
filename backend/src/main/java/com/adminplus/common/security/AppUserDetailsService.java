package com.adminplus.common.security;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.pojo.entity.UserRoleEntity;
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

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        if (user.getDeleted() != null && user.getDeleted()) {
            throw new BizException("用户已被删除");
        }

        List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(user.getId());
        List<String> roleIds = userRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .toList();

        List<RoleEntity> roles = roleIds.isEmpty() ? List.of() : roleRepository.findAllById(roleIds);
        List<String> roleCodes = roles.stream()
                .filter(role -> role.getStatus() == 1)
                .map(RoleEntity::getCode)
                .collect(Collectors.toList());

        List<String> permissions = List.of();

        return new AppUserDetails(
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
