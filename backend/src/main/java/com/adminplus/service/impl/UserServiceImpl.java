package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.enums.CommonStatus;
import com.adminplus.enums.UserStatus;
import com.adminplus.pojo.dto.query.UserQuery;
import com.adminplus.pojo.dto.request.UserCreateRequest;
import com.adminplus.pojo.dto.request.UserUpdateRequest;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.pojo.dto.response.UserResponse;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.pojo.entity.UserRoleEntity;
import com.adminplus.repository.DeptRepository;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.DeptService;
import com.adminplus.service.UserService;
import com.adminplus.utils.AssociationDiffHelper;
import com.adminplus.utils.EntityHelper;
import com.adminplus.utils.PageUtils;
import com.adminplus.utils.PasswordUtils;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.utils.XssUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author AdminPlus
 * @since 2026-02-06
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final DeptRepository deptRepository;
    private final DeptService deptService;
    private final PasswordEncoder passwordEncoder;
    private final ConversionService conversionService;

    @Override
    @Transactional(readOnly = true)
    public PageResultResponse<UserResponse> getUserList(UserQuery query) {
        var pageable = PageUtils.toPageable(query.getPage(), query.getSize());

        Page<UserEntity> pageResult = queryUsers(pageable, query.getKeyword(), query.getDeptId(), query.getStatus());

        // 批量预查询避免 N+1 问题
        List<UserEntity> users = pageResult.getContent();
        Map<String, String> deptNameMap = batchGetDeptNames(users);
        Map<String, List<String>> userRolesMap = batchGetUserRoles(users);

        return PageUtils.toResp(pageResult, user -> buildUserResponse(user, deptNameMap, userRolesMap));
    }

    /**
     * 批量获取部门名称
     */
    private Map<String, String> batchGetDeptNames(List<UserEntity> users) {
        List<String> deptIds = users.stream()
                .map(UserEntity::getDeptId)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .toList();

        if (deptIds.isEmpty()) {
            return Map.of();
        }

        return deptRepository.findAllById(deptIds).stream()
                .collect(Collectors.toMap(dept -> dept.getId(), dept -> dept.getName()));
    }

    /**
     * 批量获取用户角色名称
     */
    private Map<String, List<String>> batchGetUserRoles(List<UserEntity> users) {
        List<String> userIds = users.stream()
                .map(UserEntity::getId)
                .toList();

        if (userIds.isEmpty()) {
            return Map.of();
        }

        // 批量查询用户角色关联
        List<UserRoleEntity> userRoles = userRoleRepository.findByUserIdIn(userIds);

        // 收集所有角色ID
        List<String> roleIds = userRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .distinct()
                .toList();

        // 批量查询角色
        Map<String, String> roleNameMap = roleRepository.findAllById(roleIds).stream()
                .collect(Collectors.toMap(RoleEntity::getId, RoleEntity::getName));

        // 按 userId 分组
        return userRoles.stream()
                .filter(ur -> roleNameMap.containsKey(ur.getRoleId()))
                .collect(Collectors.groupingBy(
                        UserRoleEntity::getUserId,
                        Collectors.mapping(ur -> roleNameMap.get(ur.getRoleId()), Collectors.toList())
                ));
    }

    /**
     * 构建用户响应（使用预查询数据）
     */
    private UserResponse buildUserResponse(UserEntity user, Map<String, String> deptNameMap, Map<String, List<String>> userRolesMap) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getStatus(),
                user.getDeptId(),
                deptNameMap.get(user.getDeptId()),
                userRolesMap.getOrDefault(user.getId(), List.of()),
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }

    /**
     * 根据条件查询用户
     */
    private Page<UserEntity> queryUsers(Pageable pageable, String keyword, String deptId, Integer status) {
        boolean isAdmin = SecurityUtils.isAdmin();
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasDeptId = deptId != null && !deptId.isEmpty();
        boolean hasStatus = status != null;

        // 管理员查询 - 有完整权限
        if (isAdmin) {
            return queryUsersAsAdmin(pageable, keyword, deptId, status, hasKeyword, hasDeptId, hasStatus);
        }

        // 非管理员查询 - 受当前用户部门权限限制
        return queryUsersAsRegularUser(pageable, keyword, status, hasKeyword);
    }

    /**
     * 管理员查询用户
     */
    private Page<UserEntity> queryUsersAsAdmin(Pageable pageable, String keyword, String deptId,
                                                Integer status, boolean hasKeyword,
                                                boolean hasDeptId, boolean hasStatus) {
        if (hasKeyword && hasDeptId && hasStatus) {
            List<String> deptIds = deptService.getDeptAndChildrenIds(deptId);
            return userRepository.findByKeywordAndDeptIdInAndStatus(keyword.trim(), deptIds, status, pageable);
        } else if (hasKeyword && hasDeptId) {
            List<String> deptIds = deptService.getDeptAndChildrenIds(deptId);
            return userRepository.findByKeywordAndDeptIdIn(keyword.trim(), deptIds, pageable);
        } else if (hasKeyword && hasStatus) {
            return userRepository.findByKeywordAndStatus(keyword.trim(), status, pageable);
        } else if (hasKeyword) {
            return userRepository.findByKeyword(keyword.trim(), pageable);
        } else if (hasDeptId && hasStatus) {
            List<String> deptIds = deptService.getDeptAndChildrenIds(deptId);
            return userRepository.findByDeptIdInAndStatus(deptIds, status, pageable);
        } else if (hasDeptId) {
            List<String> deptIds = deptService.getDeptAndChildrenIds(deptId);
            return userRepository.findByDeptIdIn(deptIds, pageable);
        } else if (hasStatus) {
            return userRepository.findByStatus(status, pageable);
        } else {
            return userRepository.findAll(pageable);
        }
    }

    /**
     * 普通用户查询 - 只能查看本部门及子部门用户
     */
    private Page<UserEntity> queryUsersAsRegularUser(Pageable pageable, String keyword,
                                                      Integer status, boolean hasKeyword) {
        String currentDeptId = SecurityUtils.getCurrentUserDeptId();
        if (currentDeptId == null) {
            return Page.empty(pageable);
        }

        List<String> accessibleDeptIds = deptService.getDeptAndChildrenIds(currentDeptId);

        if (hasKeyword && status != null) {
            return userRepository.findByKeywordAndDeptIdInAndStatus(keyword.trim(), accessibleDeptIds, status, pageable);
        } else if (hasKeyword) {
            return userRepository.findByKeywordAndDeptIdIn(keyword.trim(), accessibleDeptIds, pageable);
        } else if (status != null) {
            return userRepository.findByDeptIdInAndStatus(accessibleDeptIds, status, pageable);
        } else {
            return userRepository.findByDeptIdIn(accessibleDeptIds, pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(String id) {
        var user = EntityHelper.findByIdOrThrow(userRepository::findById, id, "用户不存在");
        return conversionService.convert(user, UserResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BizException("用户不存在"));
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest req) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(req.username())) {
            throw new BizException("用户名已存在");
        }

        // 验证密码强度
        if (!PasswordUtils.isStrongPassword(req.password())) throw new BizException(PasswordUtils.getErrorMessage(PasswordUtils.getPasswordStrengthHint(req.password())));

        // 验证部门是否存在
        if (req.deptId() != null && !req.deptId().isEmpty()) {
            if (!deptRepository.existsById(req.deptId())) {
                throw new BizException("部门不存在");
            }
        }

        var user = new UserEntity();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setNickname(XssUtils.escapeOrNull(req.nickname()));
        user.setEmail(XssUtils.escapeOrNull(req.email()));
        user.setPhone(XssUtils.escapeOrNull(req.phone()));
        user.setAvatar(req.avatar());
        user.setDeptId(req.deptId());
        user.setStatus(UserStatus.ENABLED.getCode());

        user = userRepository.save(user);

        return conversionService.convert(user, UserResponse.class);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        var user = EntityHelper.findByIdOrThrow(userRepository::findById, id, "用户不存在");

        if (request.nickname() != null) {
            user.setNickname(XssUtils.escapeOrNull(request.nickname()));
        }
        if (request.email() != null) {
            user.setEmail(XssUtils.escapeOrNull(request.email()));
        }
        if (request.phone() != null) {
            user.setPhone(XssUtils.escapeOrNull(request.phone()));
        }
        if (request.avatar() != null) {
            user.setAvatar(request.avatar());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }
        if (request.deptId() != null) {
            // 验证部门是否存在
            if (!request.deptId().isEmpty() && !deptRepository.existsById(request.deptId())) {
                throw new BizException("部门不存在");
            }
            user.setDeptId(request.deptId().isEmpty() ? null : request.deptId());
        }

        user = userRepository.save(user);

        return conversionService.convert(user, UserResponse.class);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        // 不能删除自己
        if (id.equals(SecurityUtils.getCurrentUserIdOrDefault())) {
            throw new BizException("不能删除自己");
        }

        var user = EntityHelper.findByIdOrThrow(userRepository::findById, id, "用户不存在");

        // 逻辑删除（Entity 配置了 @SQLDelete，delete() 会触发 UPDATE SET deleted=true）
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void updateUserStatus(String id, Integer status) {
        // 不能禁用自己
        if (id.equals(SecurityUtils.getCurrentUserIdOrDefault()) && CommonStatus.isDisabled(status)) {
            throw new BizException("不能禁用自己");
        }

        // 校验 status 值范围
        CommonStatus.fromCode(status);  // 会抛出异常如果 status 不合法

        var user = EntityHelper.findByIdOrThrow(userRepository::findById, id, "用户不存在");

        user.setStatus(status);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(String id, String newPassword) {
        var user = EntityHelper.findByIdOrThrow(userRepository::findById, id, "用户不存在");

        // 验证新密码强度（确保所有密码修改操作都使用相同的密码强度规则）
        if (!PasswordUtils.isStrongPassword(newPassword)) throw new BizException(PasswordUtils.getErrorMessage(PasswordUtils.getPasswordStrengthHint(newPassword)));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void assignRoles(String userId, List<String> roleIds) {
        // 检查用户是否存在
        userRepository.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));

        // 验证角色是否都存在
        List<String> safeRoleIds = (roleIds != null) ? roleIds : List.of();
        if (!safeRoleIds.isEmpty()) {
            List<RoleEntity> foundRoles = roleRepository.findAllById(safeRoleIds);
            if (foundRoles.size() != safeRoleIds.size()) {
                List<String> foundIds = foundRoles.stream().map(RoleEntity::getId).toList();
                String missingId = safeRoleIds.stream()
                        .filter(id -> !foundIds.contains(id))
                        .findFirst()
                        .orElse(null);
                throw new BizException("角色不存在，ID: " + missingId);
            }
        }

        // diff 精准更新
        AssociationDiffHelper.diffUpdate(
                userId,
                safeRoleIds,
                uid -> userRoleRepository.findByUserId(uid).stream()
                        .map(UserRoleEntity::getRoleId)
                        .collect(Collectors.toSet()),
                userRoleRepository::deleteByUserIdAndRoleIdIn,
                (uid, toAdd) -> {
                    List<UserRoleEntity> list = toAdd.stream().map(roleId -> {
                        var e = new UserRoleEntity();
                        e.setUserId(uid);
                        e.setRoleId(roleId);
                        return e;
                    }).toList();
                    userRoleRepository.saveAll(list);
                }
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserRoleIds(String userId) {
        // 检查用户是否存在
        userRepository.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));
        return userRoleRepository.findByUserId(userId).stream()
                .map(UserRoleEntity::getRoleId)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserRoleNames(String userId) {
        return getActiveRoles(userId).stream()
                .map(RoleEntity::getName)
                .toList();
    }

    /**
     * 获取用户的启用状态角色列表
     */
    private List<RoleEntity> getActiveRoles(String userId) {
        return roleRepository.findActiveRolesByUserId(userId);
    }
}