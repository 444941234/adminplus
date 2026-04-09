package com.adminplus.converter.profile;

import com.adminplus.pojo.dto.response.ProfileResponse;
import com.adminplus.pojo.entity.DeptEntity;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.pojo.entity.UserRoleEntity;
import com.adminplus.repository.DeptRepository;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * UserEntity → ProfileResponse 转换器
 * <p>
 * 自动查询 deptName 和 roles
 */
@Component
@RequiredArgsConstructor
public class ProfileConverter implements Converter<UserEntity, ProfileResponse> {

    private final DeptRepository deptRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @Override
    public ProfileResponse convert(UserEntity source) {
        // 查询部门名称
        String deptName = null;
        if (source.getDeptId() != null) {
            deptName = deptRepository.findById(source.getDeptId())
                    .map(DeptEntity::getName)
                    .orElse(null);
        }

        // 查询角色名称列表
        List<String> roles = null;
        if (source.getId() != null) {
            List<String> roleIds = userRoleRepository.findByUserId(source.getId())
                    .stream()
                    .map(UserRoleEntity::getRoleId)
                    .toList();
            if (!roleIds.isEmpty()) {
                roles = roleRepository.findAllById(roleIds)
                        .stream()
                        .map(RoleEntity::getName)
                        .toList();
            }
        }

        return new ProfileResponse(
                source.getId(),
                source.getUsername(),
                source.getNickname(),
                source.getEmail(),
                source.getPhone(),
                source.getAvatar(),
                source.getStatus(),
                deptName,
                roles,
                source.getCreateTime(),
                source.getUpdateTime()
        );
    }
}