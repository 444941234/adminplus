package com.adminplus.runner.initializer;

import com.adminplus.pojo.entity.UserEntity;
import com.adminplus.pojo.entity.DeptEntity;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户数据初始化器
 *
 * @author AdminPlus
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserInitializer implements DataInitializer {

    private final UserRepository userRepository;
    private final DeptRepository deptRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public int getOrder() {
        return 4;
    }

    @Override
    public String getName() {
        return "用户数据初始化";
    }

    @Override
    @Transactional
    public void initialize() {
        if (userRepository.count() > 0) {
            log.info("用户数据已存在，跳过初始化");
            return;
        }

        // 获取部门数据
        List<DeptEntity> depts = deptRepository.findAll();
        Map<String, DeptEntity> deptMap = depts.stream()
                .collect(Collectors.toMap(DeptEntity::getCode, d -> d));

        // 创建用户数据
        String encodedPassword = passwordEncoder.encode("admin123");

        List<UserEntity> users = new ArrayList<>();

        // 超级管理员（总部）
        users.add(createUser("admin", "超级管理员", "admin@adminplus.com", "13800000001",
                deptMap.get("HQ").getId(), encodedPassword));

        // 部门经理（技术研发部）
        users.add(createUser("manager", "部门经理", "manager@adminplus.com", "13800000002",
                deptMap.get("RD").getId(), encodedPassword));

        // 开发人员（后端开发组）
        users.add(createUser("dev1", "开发人员1", "dev1@adminplus.com", "13800000003",
                deptMap.get("BE").getId(), encodedPassword));
        users.add(createUser("dev2", "开发人员2", "dev2@adminplus.com", "13800000004",
                deptMap.get("FE").getId(), encodedPassword));

        // 运营人员（市场推广组）
        users.add(createUser("operator1", "运营人员1", "operator1@adminplus.com", "13800000005",
                deptMap.get("MP").getId(), encodedPassword));
        users.add(createUser("operator2", "运营人员2", "operator2@adminplus.com", "13800000006",
                deptMap.get("MP").getId(), encodedPassword));

        // 客服人员（客户服务组）
        users.add(createUser("cs1", "客服人员1", "cs1@adminplus.com", "13800000007",
                deptMap.get("CS").getId(), encodedPassword));
        users.add(createUser("cs2", "客服人员2", "cs2@adminplus.com", "13800000008",
                deptMap.get("CS").getId(), encodedPassword));

        // 普通用户
        users.add(createUser("user1", "普通用户1", "user1@adminplus.com", "13800000009",
                deptMap.get("HQ").getId(), encodedPassword));
        users.add(createUser("user2", "普通用户2", "user2@adminplus.com", "13800000010",
                deptMap.get("HQ").getId(), encodedPassword));

        userRepository.saveAll(users);

        // 获取 admin 用户 ID，更新所有用户的 createUser/updateUser
        UserEntity adminUser = users.stream()
                .filter(u -> "admin".equals(u.getUsername()))
                .findFirst()
                .orElse(null);

        if (adminUser != null) {
            String adminId = adminUser.getId();
            for (UserEntity user : users) {
                user.setCreateUser(adminId);
                user.setUpdateUser(adminId);
            }
            userRepository.saveAll(users);

            // 更新部门的 createUser/updateUser
            for (DeptEntity dept : depts) {
                dept.setCreateUser(adminId);
                dept.setUpdateUser(adminId);
            }
            deptRepository.saveAll(depts);

            // 更新角色的 createUser/updateUser
            List<RoleEntity> roles = roleRepository.findAll();
            for (RoleEntity role : roles) {
                role.setCreateUser(adminId);
                role.setUpdateUser(adminId);
            }
            roleRepository.saveAll(roles);
        }

        log.info("初始化用户数据完成，共 {} 个用户，已更新部门和角色的创建者", users.size());
    }

    private UserEntity createUser(String username, String nickname, String email, String phone,
                                  String deptId, String encodedPassword) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus(1);
        user.setDeptId(deptId);
        return user;
    }
}