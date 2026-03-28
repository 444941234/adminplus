package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.constants.OperationType;
import com.adminplus.pojo.dto.req.DeptCreateReq;
import com.adminplus.pojo.dto.req.DeptUpdateReq;
import com.adminplus.pojo.dto.resp.DeptResp;
import com.adminplus.pojo.entity.DeptEntity;
import com.adminplus.repository.DeptRepository;
import com.adminplus.service.DeptService;
import com.adminplus.service.LogService;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.utils.TreeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门服务实现
 *
 * @author AdminPlus
 * @since 2026-02-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptRepository deptRepository;
    private final LogService logService;

    @Override
    @Transactional(readOnly = true)
    public List<DeptResp> getDeptTree() {
        List<DeptEntity> allDepts;

        // 超级管理员可以查看所有部门
        if (SecurityUtils.isAdmin()) {
            allDepts = deptRepository.findAllByOrderBySortOrderAsc();
        } else {
            // 非超级管理员只能查看本部门及以下部门
            String currentDeptId = SecurityUtils.getCurrentUserDeptId();
            if (currentDeptId == null) {
                return List.of();
            }

            // 获取本部门及所有子部门
            List<String> accessibleDeptIds = getDeptAndChildrenIds(currentDeptId);

            // 对于非根部门，需要调整其 parentId 为 "0" 以便构建树
            // 这样用户的当前部门会成为树的根节点
            allDepts = deptRepository.findAllById(accessibleDeptIds);

            // 按sortOrder排序
            allDepts.sort((a, b) -> {
                if (a.getSortOrder() == null && b.getSortOrder() == null) return 0;
                if (a.getSortOrder() == null) return 1;
                if (b.getSortOrder() == null) return -1;
                return a.getSortOrder().compareTo(b.getSortOrder());
            });
        }

        // 转换为 VO（扁平结构，children 为 null）
        List<DeptResp> deptResps = allDepts.stream()
                .map(dept -> {
                    DeptResp resp = toResp(dept);
                    // 对于非管理员用户，如果当前部门不是根部门（parentId != "0"），
                    // 则将其 parentId 改为 "0"，使其成为树的根节点
                    if (!SecurityUtils.isAdmin() && dept.getId().equals(SecurityUtils.getCurrentUserDeptId())) {
                        if (resp.parentId() != null && !resp.parentId().equals("0")) {
                            // 创建新的 DeptResp，parentId 设为 "0"
                            return new DeptResp(
                                    resp.id(),
                                    "0",  // 设为根节点
                                    resp.name(),
                                    resp.code(),
                                    resp.leader(),
                                    resp.phone(),
                                    resp.email(),
                                    resp.sortOrder(),
                                    resp.status(),
                                    resp.children(),
                                    resp.createTime(),
                                    resp.updateTime()
                            );
                        }
                    }
                    return resp;
                })
                .toList();

        // 使用 TreeUtils.buildTreeForRecord 构建树形结构
        return TreeUtils.buildTreeForRecord(deptResps, this::createWithChildren);
    }

    /**
     * 创建包含子节点的新 DeptResp 实例（用于 record 类型）
     */
    private DeptResp createWithChildren(DeptResp original, List<DeptResp> children) {
        return new DeptResp(
                original.id(),
                original.parentId(),
                original.name(),
                original.code(),
                original.leader(),
                original.phone(),
                original.email(),
                original.sortOrder(),
                original.status(),
                children,
                original.createTime(),
                original.updateTime()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DeptResp getDeptById(String id) {
        var dept = deptRepository.findById(id)
                .orElseThrow(() -> new BizException("部门不存在"));

        return toResp(dept);
    }

    @Override
    @Transactional
    public DeptResp createDept(DeptCreateReq req) {
        // 检查部门名称是否已存在
        if (deptRepository.existsByNameAndDeletedFalse(req.name())) {
            throw new BizException("部门名称已存在");
        }

        // 检查部门编码是否已存在
        if (req.code() != null && !req.code().isBlank()
                && deptRepository.existsByCodeAndDeletedFalse(req.code())) {
            throw new BizException("部门编码已存在");
        }

        var dept = new DeptEntity();
        dept.setName(req.name());
        dept.setCode(req.code());
        dept.setLeader(req.leader());
        dept.setPhone(req.phone());
        dept.setEmail(req.email());
        dept.setSortOrder(req.sortOrder());
        dept.setStatus(req.status());

        // 设置父部门关系
        if (req.parentId() != null && !req.parentId().equals("0")) {
            DeptEntity parent = deptRepository.findById(req.parentId())
                    .orElseThrow(() -> new BizException("父部门不存在"));
            dept.setParent(parent);
            // 更新 ancestors
            String parentAncestors = parent.getAncestors() != null ? parent.getAncestors() : "";
            dept.setAncestors(parentAncestors + parent.getId() + ",");
        } else {
            dept.setAncestors("0,");
        }

        dept = deptRepository.save(dept);

        // 记录审计日志
        logService.log("部门管理", OperationType.CREATE, "创建部门: " + dept.getName());

        return toResp(dept);
    }

    @Override
    @Transactional
    public DeptResp updateDept(String id, DeptUpdateReq req) {
        var dept = deptRepository.findById(id)
                .orElseThrow(() -> new BizException("部门不存在"));

        // 如果更新部门名称，检查是否与其他部门重复
        if (req.name().isPresent() && !req.name().get().equals(dept.getName())) {
            if (deptRepository.existsByNameAndIdNotAndDeletedFalse(req.name().get(), id)) {
                throw new BizException("部门名称已存在");
            }
        }

        // 如果更新部门编码，检查是否与其他部门重复
        if (req.code().isPresent() && !req.code().get().equals(dept.getCode())) {
            if (deptRepository.existsByCodeAndIdNotAndDeletedFalse(req.code().get(), id)) {
                throw new BizException("部门编码已存在");
            }
        }

        req.parentId().ifPresent(parentId -> {
            // 不能将自己设置为父部门
            if (id.equals(parentId)) {
                throw new BizException("不能将自己设置为父部门");
            }
            // 检查是否将部门设置为自己的子部门（防止循环引用）
            if (isChildDept(id, parentId)) {
                throw new BizException("不能将部门设置为自己的子部门");
            }

            // 记录旧 ancestors 用于级联更新子孙
            String oldAncestors = dept.getAncestors() != null ? dept.getAncestors() : "";

            if (parentId != null && !parentId.equals("0")) {
                DeptEntity parent = deptRepository.findById(parentId)
                        .orElseThrow(() -> new BizException("父部门不存在"));
                dept.setParent(parent);
                String parentAncestors = parent.getAncestors() != null ? parent.getAncestors() : "";
                String newAncestors = parentAncestors + parent.getId() + ",";
                dept.setAncestors(newAncestors);
                // 级联更新所有子孙的 ancestors
                cascadeUpdateAncestors(oldAncestors, newAncestors, id);
            } else {
                dept.setParent(null);
                dept.setAncestors("0,");
                cascadeUpdateAncestors(oldAncestors, "0,", id);
            }
        });

        req.name().ifPresent(dept::setName);
        req.code().ifPresent(dept::setCode);
        req.leader().ifPresent(dept::setLeader);
        req.phone().ifPresent(phone -> {
            if (!phone.isBlank() && !phone.matches("^1[3-9]\\d{9}$")) {
                throw new BizException("手机号格式不正确");
            }
            dept.setPhone(phone);
        });
        req.email().ifPresent(email -> {
            if (!email.isBlank() && !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                throw new BizException("邮箱格式不正确");
            }
            dept.setEmail(email);
        });
        req.sortOrder().ifPresent(dept::setSortOrder);
        req.status().ifPresent(dept::setStatus);

        var savedDept = deptRepository.save(dept);

        // 记录审计日志
        logService.log("部门管理", OperationType.UPDATE, "更新部门: " + savedDept.getName());

        return toResp(savedDept);
    }

    @Override
    @Transactional
    public void deleteDept(String id) {
        var dept = deptRepository.findById(id)
                .orElseThrow(() -> new BizException("部门不存在"));

        // 检查是否有子部门
        if (!dept.getChildren().isEmpty()) {
            throw new BizException("该部门下存在子部门，无法删除");
        }

        deptRepository.delete(dept);

        // 记录审计日志
        logService.log("部门管理", OperationType.DELETE, "删除部门: " + dept.getName());
    }

    /**
     * 检查目标部门是否是指定部门的子孙（防止循环引用）
     * <p>
     * 利用 ancestors 字段：检查 targetId 的 ancestors 路径中是否包含 parentId
     * </p>
     */
    private boolean isChildDept(String parentId, String targetId) {
        if (targetId == null || targetId.equals("0")) {
            return false;
        }
        return deptRepository.findById(targetId)
                .map(dept -> {
                    String ancestors = dept.getAncestors();
                    return ancestors != null && ancestors.contains(parentId + ",");
                })
                .orElse(false);
    }

    /**
     * 级联更新子孙节点的 ancestors 字段
     * <p>
     * 当父部门变更时，所有子孙的 ancestors 前缀需要从 oldPrefix 替换为 newPrefix
     * </p>
     *
     * @param oldAncestors 变更前的 ancestors 路径
     * @param newAncestors 变更后的 ancestors 路径
     * @param deptId       被移动的部门ID（排除自身）
     */
    private void cascadeUpdateAncestors(String oldAncestors, String newAncestors, String deptId) {
        // 查找所有子孙（ancestors 包含 oldAncestors + deptId 的节点）
        String descendantsPrefix = oldAncestors + deptId + ",";
        List<DeptEntity> descendants = deptRepository.findByAncestorsStartingWith(descendantsPrefix);

        // 构造替换后的前缀
        String newPrefix = newAncestors + deptId + ",";

        for (DeptEntity descendant : descendants) {
            String currentAncestors = descendant.getAncestors();
            if (currentAncestors != null && currentAncestors.startsWith(descendantsPrefix)) {
                String updatedAncestors = newPrefix + currentAncestors.substring(descendantsPrefix.length());
                descendant.setAncestors(updatedAncestors);
            }
        }
        deptRepository.saveAll(descendants);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDeptAndChildrenIds(String deptId) {
        if (deptId == null || deptId.isEmpty()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        result.add(deptId);

        // 构造 ancestors 前缀：当前部门的 ancestors + 自身ID
        // 例如 dept(ancestors="0,1,") -> 子孙的 ancestors 以 "0,1,deptId," 开头
        var dept = deptRepository.findById(deptId).orElse(null);
        if (dept == null) {
            return result;
        }
        String ancestorsPrefix = (dept.getAncestors() != null ? dept.getAncestors() : "") + deptId + ",";

        List<DeptEntity> descendants = deptRepository.findByAncestorsStartingWith(ancestorsPrefix);
        result.addAll(descendants.stream()
                .map(DeptEntity::getId)
                .toList());

        return result;
    }

    /**
     * 转换为响应 VO
     */
    private DeptResp toResp(DeptEntity dept) {
        String parentId = dept.getParent() != null ? dept.getParent().getId() : "0";
        return new DeptResp(
                dept.getId(),
                parentId,
                dept.getName(),
                dept.getCode(),
                dept.getLeader(),
                dept.getPhone(),
                dept.getEmail(),
                dept.getSortOrder(),
                dept.getStatus(),
                null, // children 在构建树时填充
                dept.getCreateTime(),
                dept.getUpdateTime()
        );
    }
}
