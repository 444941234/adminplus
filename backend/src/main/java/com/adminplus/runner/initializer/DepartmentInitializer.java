package com.adminplus.runner.initializer;

import com.adminplus.pojo.entity.DeptEntity;
import com.adminplus.repository.DeptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门数据初始化器
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DepartmentInitializer implements DataInitializer {

    private final DeptRepository deptRepository;

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public String getName() {
        return "部门数据初始化";
    }

    @Override
    @Transactional
    public void initialize() {
        if (deptRepository.count() > 0) {
            log.info("部门数据已存在，跳过初始化");
            return;
        }

        // 创建部门数据（临时 ID 用于建立关系）
        // 数据结构：{tempId, parentId, name, code, leader, phone, sortOrder, status}
        // sortOrder: 同级部门按此值排序，间隔10方便后续插入
        var deptData = Arrays.asList(
                new Object[]{"1", null, "AdminPlus 总部", "HQ", "张三", "010-12345678", 10, 1},
                new Object[]{"2", "1", "技术研发部", "RD", "李四", "010-12345679", 10, 1},
                new Object[]{"3", "1", "市场运营部", "MK", "王五", "010-12345680", 20, 1},
                new Object[]{"4", "2", "后端开发组", "BE", "赵六", "010-12345681", 10, 1},
                new Object[]{"5", "2", "前端开发组", "FE", "钱七", "010-12345682", 20, 1},
                new Object[]{"6", "3", "市场推广组", "MP", "孙八", "010-12345683", 10, 1},
                new Object[]{"7", "3", "客户服务组", "CS", "周九", "010-12345684", 20, 1}
        );

        // 先创建所有部门
        List<DeptEntity> depts = deptData.stream()
                .map(d -> createDept((String) d[0], (String) d[1], (String) d[2], (String) d[3],
                        (String) d[4], (String) d[5], (Integer) d[6], (Integer) d[7]))
                .toList();

        deptRepository.saveAll(depts);

        // 建立父子关系
        Map<String, DeptEntity> deptMap = depts.stream()
                .collect(Collectors.toMap(
                        dept -> getTempIdFromCode(dept.getCode()),
                        dept -> dept
                ));

        for (var d : deptData) {
            String tempId = (String) d[0];
            String parentId = (String) d[1];
            if (parentId != null && !parentId.isEmpty()) {
                DeptEntity child = deptMap.get(tempId);
                DeptEntity parent = deptMap.get(parentId);
                if (child != null && parent != null) {
                    child.setParent(parent);
                    String parentAncestors = parent.getAncestors() != null ? parent.getAncestors() : "";
                    child.setAncestors(parentAncestors + parent.getId() + ",");
                }
            }
        }

        deptRepository.saveAll(depts);
        log.info("初始化部门数据完成，共 {} 个部门", depts.size());
    }

    private DeptEntity createDept(String tempId, String parentTempId, String name, String code,
                                  String leader, String phone, Integer sortOrder, Integer status) {
        DeptEntity dept = new DeptEntity();
        dept.setName(name);
        dept.setCode(code);
        dept.setLeader(leader);
        dept.setPhone(phone);
        dept.setSortOrder(sortOrder);
        dept.setStatus(status);
        return dept;
    }

    private String getTempIdFromCode(String code) {
        return switch (code) {
            case "HQ" -> "1";
            case "RD" -> "2";
            case "MK" -> "3";
            case "BE" -> "4";
            case "FE" -> "5";
            case "MP" -> "6";
            case "CS" -> "7";
            default -> code;
        };
    }
}