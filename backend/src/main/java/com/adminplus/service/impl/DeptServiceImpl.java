package com.adminplus.service.impl;

import com.adminplus.constants.HierarchyConstants;
import com.adminplus.pojo.dto.request.DeptCreateRequest;
import com.adminplus.pojo.dto.request.DeptUpdateRequest;
import com.adminplus.pojo.dto.response.DeptResponse;
import com.adminplus.pojo.entity.DeptEntity;
import com.adminplus.repository.DeptRepository;
import com.adminplus.service.DeptService;
import com.adminplus.utils.EntityHelper;
import com.adminplus.utils.HierarchyHelper;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.utils.ServiceAssert;
import com.adminplus.utils.TreeUtils;
import com.adminplus.utils.XssUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptRepository deptRepository;
    private final ConversionService conversionService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "deptTree", key = "T(com.adminplus.utils.SecurityUtils).getCurrentUserDeptId() ?: 'admin'", unless = "#result == null || #result.isEmpty()")
    public List<DeptResponse> getDeptTree() {
        boolean isAdmin = SecurityUtils.isAdmin();
        String currentDeptId = isAdmin ? null : SecurityUtils.getCurrentUserDeptId();
        List<DeptEntity> allDepts;

        if (isAdmin) {
            allDepts = deptRepository.findAllByOrderBySortOrderAsc();
        } else {
            if (currentDeptId == null) {
                return List.of();
            }

            List<String> accessibleDeptIds = getDeptAndChildrenIds(currentDeptId);
            allDepts = deptRepository.findAllById(accessibleDeptIds);

            allDepts.sort((a, b) -> {
                if (a.getSortOrder() == null && b.getSortOrder() == null) return 0;
                if (a.getSortOrder() == null) return 1;
                if (b.getSortOrder() == null) return -1;
                return a.getSortOrder().compareTo(b.getSortOrder());
            });
        }

        final String finalCurrentDeptId = currentDeptId;
        List<DeptResponse> deptResponses = allDepts.stream()
                .map(dept -> {
                    DeptResponse resp = conversionService.convert(dept, DeptResponse.class);
                    if (!isAdmin && dept.getId().equals(finalCurrentDeptId)) {
                        if (resp.parentId() != null && !resp.parentId().equals(HierarchyConstants.ROOT_PARENT_ID)) {
                            return new DeptResponse(
                                    resp.id(),
                                    HierarchyConstants.ROOT_PARENT_ID,
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

        return TreeUtils.buildTreeForRecord(deptResponses, this::createWithChildren);
    }

    /**
     * 创建包含子节点的新 DeptResp 实例（用于 record 类型）
     */
    private DeptResponse createWithChildren(DeptResponse original, List<DeptResponse> children) {
        return new DeptResponse(
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
    public DeptResponse getDeptById(String id) {
        var dept = EntityHelper.findByIdOrThrow(deptRepository::findById, id, "部门不存在");

        return conversionService.convert(dept, DeptResponse.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = "deptTree", allEntries = true)
    public DeptResponse createDept(DeptCreateRequest request) {
        // 检查部门名称是否已存在
        ServiceAssert.notExists(deptRepository.existsByNameAndDeletedFalse(request.name()), "部门名称已存在");

        // 检查部门编码是否已存在
        if (request.code() != null && !request.code().isBlank()) {
            ServiceAssert.notExists(deptRepository.existsByCodeAndDeletedFalse(request.code()), "部门编码已存在");
        }

        var dept = new DeptEntity();
        dept.setName(XssUtils.escape(request.name()));
        dept.setCode(XssUtils.escape(request.code()));
        dept.setLeader(XssUtils.escape(request.leader()));
        dept.setPhone(request.phone());
        dept.setEmail(request.email());
        dept.setSortOrder(request.sortOrder());
        dept.setStatus(request.status());

        // 设置父部门关系
        if (request.parentId() != null && !request.parentId().equals(HierarchyConstants.ROOT_PARENT_ID)) {
            DeptEntity parent = EntityHelper.findByIdOrThrow(deptRepository::findById, request.parentId(), "父部门不存在");
            dept.setParent(parent);
            // 更新 ancestors
            String parentAncestors = parent.getAncestors() != null ? parent.getAncestors() : "";
            dept.setAncestors(parentAncestors + parent.getId() + ",");
        } else {
            dept.setAncestors(HierarchyConstants.ROOT_ANCESTORS);
        }

        dept = deptRepository.save(dept);

        return conversionService.convert(dept, DeptResponse.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = "deptTree", allEntries = true)
    public DeptResponse updateDept(String id, DeptUpdateRequest request) {
        var dept = EntityHelper.findByIdOrThrow(deptRepository::findById, id, "部门不存在");

        // 如果更新部门名称，检查是否与其他部门重复
        if (request.name().isPresent() && !request.name().get().equals(dept.getName())) {
            ServiceAssert.notExists(deptRepository.existsByNameAndIdNotAndDeletedFalse(request.name().get(), id), "部门名称已存在");
        }

        // 如果更新部门编码，检查是否与其他部门重复
        if (request.code().isPresent() && !request.code().get().equals(dept.getCode())) {
            ServiceAssert.notExists(deptRepository.existsByCodeAndIdNotAndDeletedFalse(request.code().get(), id), "部门编码已存在");
        }

        request.parentId().ifPresent(parentId -> {
            // 不能将自己设置为父部门
            ServiceAssert.isTrue(!id.equals(parentId), "不能将自己设置为父部门");
            // 检查是否将部门设置为自己的子部门（防止循环引用）
            ServiceAssert.isTrue(!isChildDept(id, parentId), "不能将部门设置为自己的子部门");

            // 记录旧 ancestors 用于级联更新子孙
            String oldAncestors = dept.getAncestors() != null ? dept.getAncestors() : "";

            if (!parentId.equals(HierarchyConstants.ROOT_PARENT_ID)) {
                DeptEntity parent = EntityHelper.findByIdOrThrow(deptRepository::findById, parentId, "父部门不存在");
                dept.setParent(parent);
                String parentAncestors = parent.getAncestors() != null ? parent.getAncestors() : "";
                String newAncestors = parentAncestors + parent.getId() + ",";
                dept.setAncestors(newAncestors);
                // 级联更新所有子孙的 ancestors
                cascadeUpdateAncestors(oldAncestors, newAncestors, id);
            } else {
                dept.setParent(null);
                dept.setAncestors(HierarchyConstants.ROOT_ANCESTORS);
                cascadeUpdateAncestors(oldAncestors, "0,", id);
            }
        });

        request.name().ifPresent(name -> dept.setName(XssUtils.escape(name)));
        request.code().ifPresent(code -> dept.setCode(XssUtils.escape(code)));
        request.leader().ifPresent(leader -> dept.setLeader(XssUtils.escape(leader)));
        request.phone().ifPresent(phone -> {
            ServiceAssert.isTrue(phone.isBlank() || phone.matches("^1[3-9]\\d{9}$"), "手机号格式不正确");
            dept.setPhone(phone);
        });
        request.email().ifPresent(email -> {
            ServiceAssert.isTrue(email.isBlank() || email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"), "邮箱格式不正确");
            dept.setEmail(email);
        });
        request.sortOrder().ifPresent(dept::setSortOrder);
        request.status().ifPresent(dept::setStatus);

        var savedDept = deptRepository.save(dept);

        return conversionService.convert(savedDept, DeptResponse.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = "deptTree", allEntries = true)
    public void deleteDept(String id) {
        ServiceAssert.exists(deptRepository.existsById(id), "部门不存在");
        ServiceAssert.isTrue(deptRepository.countByParentId(id) == 0, "该部门下存在子部门，无法删除");

        deptRepository.deleteById(id);
    }

    /**
     * 检查目标部门是否是指定部门的子孙（防止循环引用）
     * <p>
     * 利用 ancestors 字段：检查 targetId 的 ancestors 路径中是否包含 parentId
     * </p>
     */
    private boolean isChildDept(String parentId, String targetId) {
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
        HierarchyHelper.cascadeUpdateAncestors(
                oldAncestors, newAncestors, deptId,
                deptRepository::findByAncestorsStartingWith,
                DeptEntity::getAncestors,
                DeptEntity::setAncestors,
                deptRepository::saveAll
        );
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

    @Override
    @Transactional
    @CacheEvict(value = "deptTree", allEntries = true)
    public void updateDeptStatus(String id, Integer status) {
        var dept = EntityHelper.findByIdOrThrow(deptRepository::findById, id, "部门不存在");
        dept.setStatus(status);
        deptRepository.save(dept);
    }
}
