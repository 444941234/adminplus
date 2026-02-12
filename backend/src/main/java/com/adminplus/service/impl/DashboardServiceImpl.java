package com.adminplus.service.impl;

import com.adminplus.pojo.dto.resp.*;
import com.adminplus.pojo.entity.LogEntity;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.pojo.entity.UserRoleEntity;
import com.adminplus.repository.*;
import com.adminplus.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard 服务实现
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final LogRepository logRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public DashboardStatsResp getStats() {
        log.debug("获取 Dashboard 统计数据");

        // 统计用户数（未删除的）
        long userCount = userRepository.countByDeletedFalse();

        // 统计角色数（未删除的）
        long roleCount = roleRepository.countByDeletedFalse();

        // 统计菜单数（未删除的）
        long menuCount = menuRepository.countByDeletedFalse();

        // 统计日志数（未删除���）
        long logCount = logRepository.countByDeletedFalse();

        return new DashboardStatsResp(userCount, roleCount, menuCount, logCount);
    }

    @Override
    public ChartDataResp getUserGrowthData() {
        log.debug("获取用户增长趋势数据 - 开始");

        // 获取最近7天的日期
        List<String> dates = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.format(formatter));

            // 统计当天创建的用户数
            Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            long count = userRepository.countByCreateTimeBetweenAndDeletedFalse(startOfDay, endOfDay);
            values.add(count);
            log.debug("日期: {}, 新增用户数: {}", date, count);
        }

        log.debug("获取用户增长趋势数据 - 完成, 日期数: {}, 值数: {}", dates.size(), values.size());
        return new ChartDataResp(dates, values);
    }

    @Override
    public ChartDataResp getRoleDistributionData() {
        log.debug("获取角色分布数据 - 开始");

        List<String> roleNames = new ArrayList<>();
        List<Long> userCounts = new ArrayList<>();

        // 获取所有角色及其用户数
        List<RoleEntity> roles = roleRepository.findByDeletedFalse();
        log.debug("找到角色数: {}", roles.size());

        for (RoleEntity role : roles) {
            roleNames.add(role.getName());
            List<UserRoleEntity> userRoles = userRoleRepository.findByRoleId(role.getId());
            userCounts.add((long) userRoles.size());
            log.debug("角色: {}, 用户数: {}", role.getName(), userRoles.size());
        }

        log.debug("获取角色分布数据 - 完成, 角色数: {}", roleNames.size());
        return new ChartDataResp(roleNames, userCounts);
    }

    @Override
    public ChartDataResp getMenuDistributionData() {
        log.debug("获取菜单类型分布数据 - 开始");

        List<String> types = List.of("目录", "菜单", "按钮");
        List<Long> counts = new ArrayList<>();

        // 统计各类型菜单数量
        long directoryCount = menuRepository.countByTypeAndDeletedFalse(0);
        long menuCount = menuRepository.countByTypeAndDeletedFalse(1);
        long buttonCount = menuRepository.countByTypeAndDeletedFalse(2);

        counts.add(directoryCount);
        counts.add(menuCount);
        counts.add(buttonCount);

        log.debug("获取菜单类型分布数据 - 完成, 目录: {}, 菜单: {}, 按钮: {}",
                 directoryCount, menuCount, buttonCount);
        return new ChartDataResp(types, counts);
    }

    @Override
    public List<OperationLogResp> getRecentOperationLogs() {
        log.debug("获取最近操作日志");

        // 获取最近10条操作日志
        List<LogEntity> logs = logRepository.findTop10ByDeletedFalseOrderByCreateTimeDesc();

        List<OperationLogResp> result = new ArrayList<>();
        for (LogEntity log : logs) {
            result.add(new OperationLogResp(
                    log.getId(),
                    log.getUsername(),
                    log.getModule(),
                    log.getOperationType(),
                    log.getDescription(),
                    log.getIp(),
                    log.getCreateTime(),
                    log.getStatus(),
                    log.getCostTime()
            ));
        }

        return result;
    }

    @Override
    public SystemInfoResp getSystemInfo() {
        log.debug("获取系统信息");

        // 获取系统信息
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        // 计算内存使用情况（MB）
        long totalMemory = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long freeMemory = totalMemory - usedMemory;

        // 获取JVM运行时间
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000; // 转换为秒

        // 获取JDK版本
        String jdkVersion = System.getProperty("java.version");

        return new SystemInfoResp(
                "AdminPlus",
                "1.0.0",
                osBean.getName(),
                jdkVersion,
                totalMemory,
                usedMemory,
                freeMemory,
                "PostgreSQL",
                "16+",
                10, // 默认连接池大小
                uptime
        );
    }

    @Override
    public List<OnlineUserResp> getOnlineUsers() {
        log.debug("获取在线用户列表");

        // 获取最近登录的用户（模拟在线用户）
        List<LogEntity> recentLogs = logRepository.findTop10ByStatusAndDeletedFalseOrderByCreateTimeDesc(1);

        List<OnlineUserResp> result = new ArrayList<>();
        for (LogEntity log : recentLogs) {
            if (log.getUserId() != null) {
                result.add(new OnlineUserResp(
                        log.getUserId(),
                        log.getUsername(),
                        log.getIp(),
                        log.getCreateTime(),
                        log.getBrowser(),
                        log.getOs()
                ));
            }
        }

        return result;
    }
}