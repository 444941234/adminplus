package com.adminplus.service.workflow.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.pojo.entity.DeptEntity;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.pojo.entity.UserRoleEntity;
import com.adminplus.pojo.entity.WorkflowInstanceEntity;
import com.adminplus.pojo.entity.WorkflowNodeEntity;
import com.adminplus.repository.DeptRepository;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.workflow.WorkflowApproverResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作流审批人解析器实现
 *
 * @author AdminPlus
 * @since 2026-04-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowApproverResolverImpl implements WorkflowApproverResolver {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final DeptRepository deptRepository;

    @Override
    public List<String> resolveApprovers(WorkflowInstanceEntity instance, WorkflowNodeEntity node) {
        List<String> approvers = new ArrayList<>();

        switch (node.getApproverType()) {
            case "user":
                // 指定用户
                if (node.getApproverId() != null) {
                    approvers.add(node.getApproverId());
                }
                break;

            case "role":
                // 角色 - 查找具有该角色的所有用户
                if (node.getApproverId() != null) {
                    // approverId 可能是角色ID、角色编码或角色名称
                    String roleId = node.getApproverId();
                    RoleEntity role = null;

                    // 如果是角色编码（以 ROLE_ 开头），先查找角色
                    if (roleId.startsWith("ROLE_")) {
                        role = roleRepository.findByCode(roleId).orElse(null);
                        if (role == null) {
                            log.warn("找不到角色编码: {}", node.getApproverId());
                        }
                    } else {
                        // 尝试按名称查找角色
                        role = roleRepository.findByName(roleId).orElse(null);
                        if (role == null) {
                            // 尝试按ID查找角色
                            role = roleRepository.findById(roleId).orElse(null);
                        }
                        if (role == null) {
                            log.warn("找不到角色: {}", node.getApproverId());
                        }
                    }

                    if (role != null) {
                        roleId = role.getId();
                        List<UserRoleEntity> userRoles = userRoleRepository.findByRoleId(roleId);
                        approvers.addAll(userRoles.stream().map(UserRoleEntity::getUserId).toList());
                    }
                }
                break;

            case "dept":
            case "leader":
                // 部门/部门领导 - 查找部门的负责人
                if (instance.getDeptId() != null) {
                    DeptEntity dept = deptRepository.findById(instance.getDeptId()).orElse(null);
                    if (dept != null && dept.getLeader() != null) {
                        // leader 字段存储的是用户ID
                        approvers.add(dept.getLeader());
                    }
                }
                break;

            default:
                break;
        }

        // 如果找不到审批人，抛出异常而非静默使用 admin
        if (approvers.isEmpty()) {
            log.error("无法解析审批人: type={}, node={}", node.getApproverType(), node.getNodeName());
            throw new BizException("无法解析审批人，请联系管理员配置审批流程: " + node.getNodeName());
        }

        return approvers;
    }

    @Override
    public Map<String, String> batchGetApproverNames(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        List<String> distinctIds = userIds.stream()
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .toList();

        if (distinctIds.isEmpty()) {
            return Map.of();
        }

        return userRepository.findAllById(distinctIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getNickname));
    }
}