package com.adminplus.service;

import com.adminplus.dto.DeptCreateReq;
import com.adminplus.dto.DeptUpdateReq;
import com.adminplus.vo.DeptVO;

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
    List<DeptVO> getDeptTree();

    /**
     * 根据ID查询部门
     */
    DeptVO getDeptById(String id);

    /**
     * 创建部门
     */
    DeptVO createDept(DeptCreateReq req);

    /**
     * 更新部门
     */
    DeptVO updateDept(String id, DeptUpdateReq req);

    /**
     * 删除部门
     */
    void deleteDept(String id);
}