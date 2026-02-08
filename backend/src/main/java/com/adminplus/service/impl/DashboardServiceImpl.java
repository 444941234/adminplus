package com.adminplus.service.impl;

import com.adminplus.entity.LogEntity;
import com.adminplus.entity.MenuEntity;
import com.adminplus.entity.RoleEntity;
import com.adminplus.entity.UserEntity;
import com.adminplus.entity.UserRoleEntity;
import com.adminplus.repository.LogRepository;
import com.adminplus.repository.MenuRepository;
import com.adminplus.repository.RoleRepository;
import com.adminplus.repository.UserRepository;
import com.adminplus.repository.UserRoleRepository;
import com.adminplus.service.DashboardService;
import com.adminplus.vo.*;
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
import java.util.concurrent.TimeUnit;

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
    public DashboardStatsVO getStats() {
        log.debug("获取 Dashboard 统计数据");

        // 统计用户数（未删除的）
        long userCount = userRepository.countByDeletedFalse();

        // 统计角色数（未删除的）
        long roleCount = roleRepository.countByDeletedFalse();

        // 统计菜单数（未删除的）
        long menuCount = menuRepository.countByDeletedFalse();

        // 统计日志数（未删除���）
        long logCount = logRepository.countByDeletedFalse();

        return new DashboardStatsVO(userCount, roleCount, menuCount, logCount);
    }

    @Override
    public ChartDataVO getUserGrowthData() {
        log.debug("获取用户增长趋势数据");

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
        }

        return new ChartDataVO(dates, values);
    }

    @Override
    public ChartDataVO getRoleDistributionData() {
        log.debug("获取角色分布数据");

        List<String> roleNames = new ArrayList<>();
        List<Long> userCounts = new ArrayList<>();

        // 获取所有角色及其用户数
        List<RoleEntity> roles = roleRepository.findByDeletedFalse();
        for (RoleEntity role : roles) {
            roleNames.add(role.getRoleName());
            List<UserRoleEntity> userRoles = userRoleRepository.findByRoleId(role.getId());
            userCounts.add((long) userRoles.size());
        }

        return new ChartDataVO(roleNames, userCounts);
    }

    @Override
    public ChartDataVO getMenuDistributionData() {
        log.debug("获取菜单类型分布数据");

        List<String> types = List.of("目录", "菜单", "按钮");
        List<Long> counts = new ArrayList<>();

        // 统计各类型菜单数量
        long directoryCount = menuRepository.countByTypeAndDeletedFalse(0);
        long menuCount = menuRepository.countByTypeAndDeletedFalse(1);
        long buttonCount = menuRepository.countByTypeAndDeletedFalse(2);

        counts.add(directoryCount);
        counts.add(menuCount);
        counts.add(buttonCount);

        return new ChartDataVO(types, counts);
    }

    @Override
    public List<OperationLogVO> getRecentOperationLogs() {
        log.debug("获取最近操作日志");

        // 获取最近10条操作日志
        List<LogEntity> logs = logRepository.findTop10ByDeletedFalseOrderByCreateTimeDesc();

        List<OperationLogVO> result = new ArrayList<>();
        for (LogEntity log : logs) {
            result.add(new OperationLogVO(
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
    public SystemInfoVO getSystemInfo() {
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

        return new SystemInfoVO(
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
    public List<OnlineUserVO> getOnlineUsers() {
        log.debug("获取在线用户列表");

        // 获取最近登录的用户（模拟在线用户）
        List<LogEntity> recentLogs = logRepository.findTop10ByStatusAndDeletedFalseOrderByCreateTimeDesc(1);

        List<OnlineUserVO> result = new ArrayList<>();
        for (LogEntity log : recentLogs) {
            if (log.getUserId() != null) {
                result.add(new OnlineUserVO(
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