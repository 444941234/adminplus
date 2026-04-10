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
     *
     * @return 部门树形结构列表
     */
    List<DeptResponse> getDeptTree();

    /**
     * 根据ID查询部门
     *
     * @param id 部门ID
     * @return 部门信息
     * @throws BizException 当部门不存在时抛出
     */
    DeptResponse getDeptById(String id);

    /**
     * 创建部门
     *
     * @param request 部门创建请求
     * @return 创建的部门信息
     * @throws BizException 当父部门不存在时抛出
     */
    DeptResponse createDept(DeptCreateRequest request);

    /**
     * 更新部门
     *
     * @param id      部门ID
     * @param request 部门更新请求
     * @return 更新后的部门信息
     * @throws BizException 当部门不存在时抛出
     */
    DeptResponse updateDept(String id, DeptUpdateRequest request);

    /**
     * 删除部门
     *
     * @param id 部门ID
     * @throws BizException 当部门不存在或存在子部门时抛出
     */
    void deleteDept(String id);

    /**
     * 获取部门及其所有子部门的ID列表
     *
     * @param deptId 部门ID
     * @return 包含自身及所有子部门的ID列表
     */
    List<String> getDeptAndChildrenIds(String deptId);

    /**
     * 更新部门状态
     *
     * @param id     部门ID
     * @param status 状态值（0:禁用, 1:启用）
     * @throws BizException 当部门不存在时抛出
     */
    void updateDeptStatus(String id, Integer status);
}