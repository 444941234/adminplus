package com.adminplus.service;

import com.adminplus.pojo.dto.request.DeptCreateRequest;
import com.adminplus.pojo.dto.request.DeptUpdateRequest;
import com.adminplus.pojo.dto.response.DeptResponse;

import java.util.List;

/**
 * 部门服务接口
 *
 * @author AdminPlus
 * @since 2026-02-09
 */
public interface DeptService {

    /**
     * 查询部门树形列表
     */
    List<DeptResponse> getDeptTree();

    /**
     * 根据ID查询部门
     */
    DeptResponse getDeptById(String id);

    /**
     * 创建部门
     */
    DeptResponse createDept(DeptCreateRequest request);

    /**
     * 更新部门
     */
    DeptResponse updateDept(String id, DeptUpdateRequest request);

    /**
     * 删除部门
     */
    void deleteDept(String id);

    /**
     * 获取部门及其所有子部门的ID列表
     */
    List<String> getDeptAndChildrenIds(String deptId);

    /**
     * 更新部门状态
     */
    void updateDeptStatus(String id, Integer status);
}