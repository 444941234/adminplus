package com.adminplus.runner.initializer;

import com.adminplus.pojo.entity.ConfigEntity;
import com.adminplus.pojo.entity.ConfigGroupEntity;
import com.adminplus.repository.ConfigGroupRepository;
import com.adminplus.repository.ConfigRepository;
import com.adminplus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置数据初始化器
 *
 * @author AdminPlus
 * @since 2026-03-31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigInitializer implements DataInitializer {

    private final ConfigGroupRepository configGroupRepository;
    private final ConfigRepository configRepository;
    private final UserRepository userRepository;

    @Override
    public int getOrder() {
        return 7;
    }

    @Override
    public String getName() {
        return "配置数据初始化";
    }

    @Override
    @Transactional
    public void initialize() {
        if (configGroupRepository.count() > 0) {
            log.info("配置数据已存在，跳过初始化");
            return;
        }

        // 获取 admin 用户 ID
        String adminUserId = userRepository.findByUsername("admin")
                .map(u -> u.getId())
                .orElse("system");

        List<ConfigGroupEntity> groups = new ArrayList<>();
        List<ConfigEntity> configs = new ArrayList<>();

        // 系统配置分组
        ConfigGroupEntity systemGroup = createConfigGroup("系统配置", "system", "Settings", 1, "系统级配置参数", adminUserId);
        groups.add(systemGroup);
        configs.add(createConfig(systemGroup, "系统名称", "app.name", "AdminPlus", "STRING", "IMMEDIATE", null, "系统显示名称", true, null, 1, adminUserId));
        configs.add(createConfig(systemGroup, "系统版本", "app.version", "1.0.0", "STRING", "IMMEDIATE", null, "系统版本号", true, null, 2, adminUserId));
        configs.add(createConfig(systemGroup, "系统描述", "app.description", "AdminPlus管理系统", "STRING", "IMMEDIATE", null, "系统描述信息", false, null, 3, adminUserId));

        // 邮件配置分组
        ConfigGroupEntity mailGroup = createConfigGroup("邮件配置", "email", "Mail", 2, "邮件服务配置", adminUserId);
        groups.add(mailGroup);
        configs.add(createConfig(mailGroup, "SMTP服务器", "email.smtp.host", "smtp.example.com", "STRING", "IMMEDIATE", null, "SMTP服务器地址", true, null, 1, adminUserId));
        configs.add(createConfig(mailGroup, "SMTP端口", "email.smtp.port", "587", "NUMBER", "IMMEDIATE", "587", "SMTP服务器端口", true, null, 2, adminUserId));
        configs.add(createConfig(mailGroup, "发件人邮箱", "email.from", "noreply@example.com", "STRING", "IMMEDIATE", null, "默认发件人邮箱", true, "^\\w+@\\w+\\.\\w+$", 3, adminUserId));
        configs.add(createConfig(mailGroup, "邮件用户名", "email.username", "", "STRING", "IMMEDIATE", null, "SMTP认证用户名", false, null, 4, adminUserId));
        configs.add(createConfig(mailGroup, "邮件密码", "email.password", "", "SECRET", "IMMEDIATE", null, "SMTP认证密码", false, null, 5, adminUserId));

        // 短信配置分组
        ConfigGroupEntity smsGroup = createConfigGroup("短信配置", "sms", "MessageSquare", 3, "短信服务配置", adminUserId);
        groups.add(smsGroup);
        configs.add(createConfig(smsGroup, "短信服务商", "sms.provider", "aliyun", "STRING", "MANUAL", "aliyun", "短信服务商：aliyun/tencent", true, null, 1, adminUserId));
        configs.add(createConfig(smsGroup, "AccessKey", "sms.access.key", "", "SECRET", "IMMEDIATE", null, "短信服务AccessKey", true, null, 2, adminUserId));
        configs.add(createConfig(smsGroup, "AccessSecret", "sms.access.secret", "", "SECRET", "IMMEDIATE", null, "短信服务AccessSecret", true, null, 3, adminUserId));
        configs.add(createConfig(smsGroup, "短信签名", "sms.sign.name", "AdminPlus", "STRING", "IMMEDIATE", null, "短信签名名称", true, null, 4, adminUserId));
        configs.add(createConfig(smsGroup, "验证码模板", "sms.template.code", "SMS_123456789", "STRING", "IMMEDIATE", null, "验证码短信模板ID", true, null, 5, adminUserId));

        // 存储配置分组
        ConfigGroupEntity storageGroup = createConfigGroup("存储配置", "storage", "Database", 4, "文件存储配置", adminUserId);
        groups.add(storageGroup);
        configs.add(createConfig(storageGroup, "存储类型", "storage.type", "local", "STRING", "RESTART", "local", "存储类型：local/oss/minio", true, null, 1, adminUserId));
        configs.add(createConfig(storageGroup, "上传路径", "storage.upload.path", "/uploads", "STRING", "IMMEDIATE", "/uploads", "文件上传路径", true, null, 2, adminUserId));
        configs.add(createConfig(storageGroup, "最大文件大小", "storage.max.file.size", "10485760", "NUMBER", "IMMEDIATE", "10485760", "最大文件大小（字节）", true, null, 3, adminUserId));
        configs.add(createConfig(storageGroup, "允许的文件类型", "storage.allowed.types", "jpg,jpeg,png,gif,pdf,doc,docx", "STRING", "IMMEDIATE", null, "允许上传的文件扩展名", false, null, 4, adminUserId));

        // 安全配置分组
        ConfigGroupEntity securityGroup = createConfigGroup("安全配置", "security", "Shield", 5, "安全相关配置", adminUserId);
        groups.add(securityGroup);
        configs.add(createConfig(securityGroup, "密码最小长度", "security.password.min.length", "6", "NUMBER", "IMMEDIATE", "6", "密码最小长度", true, null, 1, adminUserId));
        configs.add(createConfig(securityGroup, "密码复杂度", "security.password.complexity", "false", "BOOLEAN", "IMMEDIATE", "false", "是否需要密码复杂度验证", true, null, 2, adminUserId));
        configs.add(createConfig(securityGroup, "登录失败锁定次数", "security.login.max.attempts", "5", "NUMBER", "IMMEDIATE", "5", "连续登录失败锁定次数", true, null, 3, adminUserId));
        configs.add(createConfig(securityGroup, "账户锁定时间", "security.login.lockout.minutes", "30", "NUMBER", "IMMEDIATE", "30", "账户锁定时间（分钟）", true, null, 4, adminUserId));
        configs.add(createConfig(securityGroup, "会话超时时间", "security.session.timeout", "7200", "NUMBER", "IMMEDIATE", "7200", "会话超时时间（秒）", true, null, 5, adminUserId));

        // 通知配置分组
        ConfigGroupEntity notifyGroup = createConfigGroup("通知配置", "notification", "Bell", 6, "消息通知配置", adminUserId);
        groups.add(notifyGroup);
        configs.add(createConfig(notifyGroup, "启用邮件通知", "notify.email.enabled", "true", "BOOLEAN", "IMMEDIATE", "true", "是否启用邮件通知", true, null, 1, adminUserId));
        configs.add(createConfig(notifyGroup, "启用短信通知", "notify.sms.enabled", "false", "BOOLEAN", "IMMEDIATE", "false", "是否启用短信通知", true, null, 2, adminUserId));
        configs.add(createConfig(notifyGroup, "启用站内通知", "notify.system.enabled", "true", "BOOLEAN", "IMMEDIATE", "true", "是否启用站内通知", true, null, 3, adminUserId));

        // 保存配置组（需要先保存以获取ID）
        configGroupRepository.saveAll(groups);

        // 更新配置项的 groupId
        for (ConfigEntity config : configs) {
            for (ConfigGroupEntity group : groups) {
                if (config.getGroupId().equals(group.getCode())) {
                    config.setGroupId(group.getId());
                    break;
                }
            }
        }

        // 保存配置项
        configRepository.saveAll(configs);

        log.info("初始化配置数据完成，共 {} 个配置分组，{} 个配置项", groups.size(), configs.size());
    }

    private ConfigGroupEntity createConfigGroup(String name, String code, String icon, Integer sortOrder, String description, String userId) {
        ConfigGroupEntity group = new ConfigGroupEntity();
        group.setName(name);
        group.setCode(code);
        group.setIcon(icon);
        group.setSortOrder(sortOrder);
        group.setDescription(description);
        group.setStatus(1);
        group.setCreateUser(userId);
        group.setUpdateUser(userId);
        return group;
    }

    private ConfigEntity createConfig(ConfigGroupEntity group, String name, String key, String value,
                                       String valueType, String effectType, String defaultValue,
                                       String description, Boolean isRequired, String validationRule, Integer sortOrder, String userId) {
        ConfigEntity config = new ConfigEntity();
        config.setGroupId(group.getCode()); // 先用 code，后面替换为 id
        config.setName(name);
        config.setKey(key);
        config.setValue(value);
        config.setValueType(valueType);
        config.setEffectType(effectType);
        config.setDefaultValue(defaultValue);
        config.setDescription(description);
        config.setIsRequired(isRequired);
        config.setValidationRule(validationRule);
        config.setSortOrder(sortOrder);
        config.setStatus(1);
        config.setCreateUser(userId);
        config.setUpdateUser(userId);
        return config;
    }
}
