package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.enums.OperationType;
import com.adminplus.enums.UserStatus;
import com.adminplus.pojo.dto.req.UserCreateReq;
import com.adminplus.pojo.dto.req.UserUpdateReq;
import com.adminplus.pojo.dto.req.LogEntry;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.pojo.dto.resp.UserResp;
import com.adminplus.pojo.entity.DeptEntity;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.pojo.entity.UserRoleEntity;
import com.adminplus.repository.DeptRepository;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.DeptService;
import com.adminplus.service.LogService;
import com.adminplus.service.UserService;
import com.adminplus.utils.AssociationDiffHelper;
import com.adminplus.utils.EntityHelper;
import com.adminplus.utils.LogMaskingUtils;
import com.adminplus.utils.PageUtils;
import com.adminplus.utils.PasswordUtils;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.utils.XssUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final DeptRepository deptRepository;
    private final DeptService deptService;
    private final PasswordEncoder passwordEncoder;
    private final LogService logService;

    @Override
    @Transactional(readOnly = true)
    public PageResultResp<UserResp> getUserList(Integer page, Integer size, String keyword, String deptId) {
        var pageable = PageUtils.toPageable(page, size);

        Page<UserEntity> pageResult = queryUsers(pageable, keyword, deptId);
        var batchData = prepareBatchData(pageResult.getContent());

        return PageUtils.toResp(pageResult, user -> toResp(user,
                batchData.deptMap.getOrDefault(user.getDeptId(), null),
                batchData.userRoleMap.getOrDefault(user.getId(), List.of())));
    }

    /**
     * 根据条件查询用户
     */
    private Page<UserEntity> queryUsers(Pageable pageable, String keyword, String deptId) {
        boolean isAdmin = SecurityUtils.isAdmin();
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if (hasKeyword && deptId != null && !deptId.isEmpty()) {
            List<String> deptIds = deptService.getDeptAndChildrenIds(deptId);
            return userRepository.findByKeywordAndDeptIdIn(keyword.trim(), deptIds, pageable);
        } else if (hasKeyword && !isAdmin) {
            String currentDeptId = SecurityUtils.getCurrentUserDeptId();
            if (currentDeptId != null) {
                List<String> accessibleDeptIds = deptService.getDeptAndChildrenIds(currentDeptId);
                return userRepository.findByKeywordAndDeptIdIn(keyword.trim(), accessibleDeptIds, pageable);
            }
            return Page.empty(pageable);
        } else if (hasKeyword) {
            return userRepository.findByKeyword(keyword.trim(), pageable);
        } else if (deptId != null && !deptId.isEmpty()) {
            List<String> deptIds = deptService.getDeptAndChildrenIds(deptId);
            return userRepository.findByDeptIdIn(deptIds, pageable);
        } else if (!isAdmin) {
            String currentDeptId = SecurityUtils.getCurrentUserDeptId();
            if (currentDeptId != null) {
                List<String> accessibleDeptIds = deptService.getDeptAndChildrenIds(currentDeptId);
                return userRepository.findByDeptIdIn(accessibleDeptIds, pageable);
            }
            return Page.empty(pageable);
        } else {
            return userRepository.findAll(pageable);
        }
    }

    /**
     * 准备批量查询数据（避免 N+1 查询）
     */
    private BatchUserData prepareBatchData(List<UserEntity> users) {
        if (users.isEmpty()) {
            return new BatchUserData(Map.of(), Map.of());
        }

        // 批量查询用户角色
        List<String> userIds = users.stream().map(UserEntity::getId).toList();
        List<UserRoleEntity> allUserRoles = userRoleRepository.findByUserIdIn(userIds);

        // 批量查询角色
        List<String> roleIds = allUserRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .distinct()
                .toList();
        Map<String, String> roleMap = roleIds.isEmpty() ? Map.of() :
                roleRepository.findAllById(roleIds).stream()
                        .collect(Collectors.toMap(RoleEntity::getId, RoleEntity::getName));

        // 批量查询部门
        List<String> deptIds = users.stream()
                .map(UserEntity::getDeptId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<String, String> deptMap = deptIds.isEmpty() ? Map.of() :
                deptRepository.findAllById(deptIds).stream()
                        .collect(Collectors.toMap(DeptEntity::getId, DeptEntity::getName));

        // 构建用户角色映射
        Map<String, List<String>> userRoleMap = new HashMap<>();
        for (UserRoleEntity ur : allUserRoles) {
            String roleName = roleMap.get(ur.getRoleId());
            if (roleName != null) {
                userRoleMap.computeIfAbsent(ur.getUserId(), k -> new ArrayList<>()).add(roleName);
            }
        }

        return new BatchUserData(userRoleMap, deptMap);
    }

    /**
     * 批量数据容器
     */
    private record BatchUserData(Map<String, List<String>> userRoleMap, Map<String, String> deptMap) {}

    @Override
    @Transactional(readOnly = true)
    public UserResp getUserById(String id) {
        var user = EntityHelper.findByIdOrThrow(userRepository::findById, id, "用户不存在");
        return buildUserRespWithRoles(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BizException("用户不存在"));
    }

    @Override
    @Transactional
    public UserResp createUser(UserCreateReq req) {
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

        // 记录审计日志
        logService.log(LogEntry.operation("用户管理", OperationType.CREATE.getCode(), "创建用户: " + user.getUsername()));

        // 查询部门名称
        String deptName = null;
        if (user.getDeptId() != null) {
            deptName = deptRepository.findById(user.getDeptId())
                    .map(DeptEntity::getName)
                    .orElse(null);
        }

        return toResp(user, deptName, List.of());
    }

    @Override
    @Transactional
    public UserResp updateUser(String id, UserUpdateReq req) {
        var user = EntityHelper.findByIdOrThrow(userRepository::findById, id, "用户不存在");

        if (req.nickname() != null) {
            user.setNickname(XssUtils.escapeOrNull(req.nickname()));
        }
        if (req.email() != null) {
            user.setEmail(XssUtils.escapeOrNull(req.email()));
        }
        if (req.phone() != null) {
            user.setPhone(XssUtils.escapeOrNull(req.phone()));
        }
        if (req.avatar() != null) {
            user.setAvatar(req.avatar());
        }
        if (req.status() != null) {
            user.setStatus(req.status());
        }
        if (req.deptId() != null) {
            // 验证部门是否存在
            if (!req.deptId().isEmpty() && !deptRepository.existsById(req.deptId())) {
                throw new BizException("部门不存在");
            }
            user.setDeptId(req.deptId().isEmpty() ? null : req.deptId());
        }

        user = userRepository.save(user);

        // 记录审计日志
        logService.log(LogEntry.operation("用户管理", OperationType.UPDATE.getCode(), "更新用户: " + user.getUsername()));

        // 查询部门名称
        String deptName = null;
        if (user.getDeptId() != null) {
            deptName = deptRepository.findById(user.getDeptId())
                    .map(DeptEntity::getName)
                    .orElse(null);
        }

        // 查询用户角色
        List<UserRoleEntity> updateUserRoles = userRoleRepository.findByUserId(id);
        List<String> roleIds = updateUserRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .toList();
        Map<String, String> roleMap = roleRepository.findAllById(roleIds).stream()
                .collect(Collectors.toMap(RoleEntity::getId, RoleEntity::getName));
        List<String> roleNames = updateUserRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .map(roleMap::get)
                .filter(Objects::nonNull)
                .toList();

        return toResp(user, deptName, roleNames);
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

        // 记录审计日志
        logService.log(LogEntry.operation("用户管理", OperationType.DELETE.getCode(), "删除用户: " + user.getUsername()));
    }

    @Override
    @Transactional
    public void updateUserStatus(String id, Integer status) {
        // 不能禁用自己
        if (id.equals(SecurityUtils.getCurrentUserIdOrDefault()) && status == 0) {
            throw new BizException("不能禁用自己");
        }

        // 校验 status 值范围
        if (status != null && status != 0 && status != 1) {
            throw new BizException("状态值不合法，只能为 0 或 1");
        }

        var user = EntityHelper.findByIdOrThrow(userRepository::findById, id, "用户不存在");

        user.setStatus(status);
        userRepository.save(user);

        // 记录审计日志
        logService.log(LogEntry.operation("用户管理", OperationType.UPDATE.getCode(), "更新用户状态: " + user.getUsername() + " -> " + status));
    }

    @Override
    @Transactional
    public void resetPassword(String id, String newPassword) {
        var user = EntityHelper.findByIdOrThrow(userRepository::findById, id, "用户不存在");

        // 验证新密码强度（确保所有密码修改操作都使用相同的密码强度规则）
        if (!PasswordUtils.isStrongPassword(newPassword)) throw new BizException(PasswordUtils.getErrorMessage(PasswordUtils.getPasswordStrengthHint(newPassword)));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 记录审计日志（使用掩码隐藏用户名）
        logService.log(LogEntry.operation("用户管理", OperationType.UPDATE.getCode(), "重置密码: " + LogMaskingUtils.maskUsername(user.getUsername())));
    }

    @Override
    @Transactional
    public void assignRoles(String userId, List<String> roleIds) {
        // 检查用户是否存在
        var user = userRepository.findById(userId)
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
        var result = AssociationDiffHelper.diffUpdate(
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

        // 审计日志
        if (result.hasChanges()) {
            String roleNames = safeRoleIds.isEmpty() ? "" :
                    roleRepository.findAllById(safeRoleIds).stream()
                            .map(RoleEntity::getName)
                            .collect(Collectors.joining(", "));
            logService.log(LogEntry.operation("用户管理", OperationType.UPDATE.getCode(),
                    "分配角色: " + user.getUsername() + " -> " + roleNames
                    + " (新增" + result.added() + "个, 移除" + result.removed() + "个)"));
        }
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
    public UserResp getUserRespByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BizException("用户不存在"));

        return buildUserRespWithRoles(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserRoleCodes(String userId) {
        return getActiveRoles(userId).stream()
                .map(RoleEntity::getCode)
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
        List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(userId);
        if (userRoles.isEmpty()) {
            return List.of();
        }

        List<String> roleIds = userRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .toList();

        Map<String, RoleEntity> roleMap = roleRepository.findAllById(roleIds).stream()
                .collect(Collectors.toMap(RoleEntity::getId, r -> r));

        return userRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .map(roleMap::get)
                .filter(Objects::nonNull)
                .filter(role -> role.getStatus() == 1)
                .toList();
    }

    /**
     * 构建用户响应对象（包含角色信息）
     */
    private UserResp buildUserRespWithRoles(UserEntity user) {
        List<RoleEntity> roles = getActiveRoles(user.getId());
        List<String> roleNames = roles.stream()
                .map(RoleEntity::getName)
                .toList();

        // 查询部门名称
        String deptName = null;
        if (user.getDeptId() != null) {
            deptName = deptRepository.findById(user.getDeptId())
                    .map(DeptEntity::getName)
                    .orElse(null);
        }

        return toResp(user, deptName, roleNames);
    }

    private UserResp toResp(UserEntity user, String deptName, List<String> roleNames) {
        return new UserResp(
                user.getId(), user.getUsername(), user.getNickname(),
                user.getEmail(), user.getPhone(), user.getAvatar(),
                user.getStatus(), user.getDeptId(), deptName, roleNames,
                user.getCreateTime(), user.getUpdateTime()
        );
    }
}