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
import java.util.stream.Collectors;

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
            List<String> accessibleDeptIds = getDeptAndChildrenIds(currentDeptId);
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
        List<DeptResp> deptResps = allDepts.stream().map(this::toResp).toList();

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

        req.parentId().ifPresent(parentId -> {
            // 不能将自己设置为父部门
            if (id.equals(parentId)) {
                throw new BizException("不能将自己设置为父部门");
            }
            // 检查是否将部门设置为自己的子部门（防止循环引用）
            if (isChildDept(id, parentId)) {
                throw new BizException("不能将部门设置为自己的子部门");
            }
            if (parentId != null && !parentId.equals("0")) {
                DeptEntity parent = deptRepository.findById(parentId)
                        .orElseThrow(() -> new BizException("父部门不存在"));
                dept.setParent(parent);
                // 更新 ancestors
                String parentAncestors = parent.getAncestors() != null ? parent.getAncestors() : "";
                dept.setAncestors(parentAncestors + parent.getId() + ",");
            } else {
                dept.setParent(null);
                dept.setAncestors("0,");
            }
        });

        req.name().ifPresent(dept::setName);
        req.code().ifPresent(dept::setCode);
        req.leader().ifPresent(dept::setLeader);
        req.phone().ifPresent(dept::setPhone);
        req.email().ifPresent(dept::setEmail);
        req.sortOrder().ifPresent(dept::setSortOrder);
        req.status().ifPresent(dept::setStatus);

        var savedDept = deptRepository.save(dept);

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
     * 检查目标部门是否是指定部门的子部门（防止循环引用）
     */
    private boolean isChildDept(String parentId, String targetId) {
        if (targetId == null || targetId.equals("0")) {
            return false;
        }

        List<DeptEntity> allDepts = deptRepository.findAllByOrderBySortOrderAsc();

        // 从目标部门开始向上查找
        String currentId = targetId;
        while (currentId != null && !currentId.equals("0")) {
            if (currentId.equals(parentId)) {
                return true;
            }

            final String finalCurrentId = currentId;
            DeptEntity currentDept = allDepts.stream()
                    .filter(d -> d.getId().equals(finalCurrentId))
                    .findFirst()
                    .orElse(null);

            if (currentDept == null) {
                break;
            }

            // 使用 parent 对象获取父节点 ID
            DeptEntity parent = currentDept.getParent();
            currentId = parent != null ? parent.getId() : null;
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDeptAndChildrenIds(String deptId) {
        if (deptId == null || deptId.isEmpty()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        result.add(deptId);

        // 递归获取所有子部门ID
        List<String> parentIds = List.of(deptId);
        while (!parentIds.isEmpty()) {
            List<DeptEntity> children = deptRepository.findByParentIdInOrderBySortOrderAsc(parentIds);
            if (children.isEmpty()) {
                break;
            }
            List<String> childIds = children.stream()
                    .map(DeptEntity::getId)
                    .toList();
            result.addAll(childIds);
            parentIds = childIds;
        }

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
