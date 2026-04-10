package com.adminplus.common.security;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.repository.MenuRepository;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        if (user.getDeleted() != null && user.getDeleted()) {
            throw new BizException("用户已被删除");
        }

        // 直接通过 userId 查询启用的角色 code 和权限（关联查询优化）
        List<String> roleCodes = roleRepository.findActiveRoleCodesByUserId(user.getId());
        List<String> permissions = menuRepository.findPermKeysByUserId(user.getId());

        return new AppUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getDeptId(),
                user.getStatus(),
                roleCodes,
                permissions
        );
    }
}
