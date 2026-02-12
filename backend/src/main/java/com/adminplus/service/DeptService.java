package com.adminplus.service;

import com.adminplus.pojo.dto.req.DeptCreateReq;
import com.adminplus.pojo.dto.req.DeptUpdateReq;
import com.adminplus.pojo.dto.resp.DeptResp;

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
    List<DeptResp> getDeptTree();

    /**
     * 根据ID查询部门
     */
    DeptResp getDeptById(String id);

    /**
     * 创建部门
     */
    DeptResp createDept(DeptCreateReq req);

    /**
     * 更新部门
     */
    DeptResp updateDept(String id, DeptUpdateReq req);

    /**
     * 删除部门
     */
    void deleteDept(String id);

    /**
     * 获取部门及其所有子部门的ID列表
     */
    List<String> getDeptAndChildrenIds(String deptId);
}