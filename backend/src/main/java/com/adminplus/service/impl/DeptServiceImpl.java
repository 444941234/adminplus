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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        List<DeptEntity> allDepts = deptRepository.findAllByOrderBySortOrderAsc();

        // 转换为 VO
        List<DeptResp> deptResps = allDepts.stream().map(dept -> new DeptResp(
                dept.getId(),
                dept.getParentId(),
                dept.getName(),
                dept.getCode(),
                dept.getLeader(),
                dept.getPhone(),
                dept.getEmail(),
                dept.getSortOrder(),
                dept.getStatus(),
                null, // children 稍后填充
                dept.getCreateTime(),
                dept.getUpdateTime()
        )).toList();

        // 构建树形结构
        return buildTreeWithChildren(deptResps, null);
    }

    /**
     * 构建树形结构（带 children）
     */
    private List<DeptResp> buildTreeWithChildren(List<DeptResp> depts, String parentId) {
        Map<String, List<DeptResp>> childrenMap = depts.stream()
                .filter(dept -> dept.parentId() != null && !dept.parentId().equals("0"))
                .collect(Collectors.groupingBy(DeptResp::parentId));

        return depts.stream()
                .filter(dept -> {
                    if (parentId == null) {
                        return dept.parentId() == null || dept.parentId().equals("0");
                    }
                    return parentId.equals(dept.parentId());
                })
                .map(dept -> {
                    List<DeptResp> children = childrenMap.getOrDefault(dept.id(), new ArrayList<>());
                    // 递归构建子节点
                    List<DeptResp> childTree = buildTreeWithChildren(depts, dept.id());
                    if (!childTree.isEmpty()) {
                        children = childTree;
                    }
                    return new DeptResp(
                            dept.id(),
                            dept.parentId(),
                            dept.name(),
                            dept.code(),
                            dept.leader(),
                            dept.phone(),
                            dept.email(),
                            dept.sortOrder(),
                            dept.status(),
                            children,
                            dept.createTime(),
                            dept.updateTime()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DeptResp getDeptById(String id) {
        var dept = deptRepository.findById(id)
                .orElseThrow(() -> new BizException("部门不存在"));

        return new DeptResp(
                dept.getId(),
                dept.getParentId(),
                dept.getName(),
                dept.getCode(),
                dept.getLeader(),
                dept.getPhone(),
                dept.getEmail(),
                dept.getSortOrder(),
                dept.getStatus(),
                null,
                dept.getCreateTime(),
                dept.getUpdateTime()
        );
    }

    @Override
    @Transactional
    public DeptResp createDept(DeptCreateReq req) {
        // 检查部门名称是否已存在
        if (deptRepository.existsByNameAndDeletedFalse(req.name())) {
            throw new BizException("部门名称已存在");
        }

        // 如果有父部门，检查父部门是否存在
        if (req.parentId() != null && !req.parentId().equals("0")) {
            if (!deptRepository.existsById(req.parentId())) {
                throw new BizException("父部门不存在");
            }
        }

        var dept = new DeptEntity();
        dept.setParentId(req.parentId());
        dept.setName(req.name());
        dept.setCode(req.code());
        dept.setLeader(req.leader());
        dept.setPhone(req.phone());
        dept.setEmail(req.email());
        dept.setSortOrder(req.sortOrder());
        dept.setStatus(req.status());

        dept = deptRepository.save(dept);

        // 记录审计日志
        logService.log("部门管理", OperationType.CREATE, "创建部门: " + dept.getName());

        return new DeptResp(
                dept.getId(),
                dept.getParentId(),
                dept.getName(),
                dept.getCode(),
                dept.getLeader(),
                dept.getPhone(),
                dept.getEmail(),
                dept.getSortOrder(),
                dept.getStatus(),
                null,
                dept.getCreateTime(),
                dept.getUpdateTime()
        );
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
                if (!deptRepository.existsById(parentId)) {
                    throw new BizException("父部门不存在");
                }
            }
            dept.setParentId(parentId);
        });

        req.name().ifPresent(dept::setName);
        req.code().ifPresent(dept::setCode);
        req.leader().ifPresent(dept::setLeader);
        req.phone().ifPresent(dept::setPhone);
        req.email().ifPresent(dept::setEmail);
        req.sortOrder().ifPresent(dept::setSortOrder);
        req.status().ifPresent(dept::setStatus);

        var savedDept = deptRepository.save(dept);

        return new DeptResp(
                savedDept.getId(),
                savedDept.getParentId(),
                savedDept.getName(),
                savedDept.getCode(),
                savedDept.getLeader(),
                savedDept.getPhone(),
                savedDept.getEmail(),
                savedDept.getSortOrder(),
                savedDept.getStatus(),
                null,
                savedDept.getCreateTime(),
                savedDept.getUpdateTime()
        );
    }

    @Override
    @Transactional
    public void deleteDept(String id) {
        var dept = deptRepository.findById(id)
                .orElseThrow(() -> new BizException("部门不存在"));

        // 检查是否有子部门
        List<DeptEntity> children = deptRepository.findByParentIdOrderBySortOrderAsc(id);

        if (!children.isEmpty()) {
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

            currentId = currentDept.getParentId();
        }

        return false;
    }
}