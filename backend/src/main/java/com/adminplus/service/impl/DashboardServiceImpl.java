package com.adminplus.service.impl;

import com.adminplus.constants.DateTimeConstants;
import com.adminplus.constants.DashboardConstants;
import com.adminplus.pojo.dto.response.*;
import com.adminplus.pojo.entity.LogEntity;
import com.adminplus.pojo.entity.RefreshTokenEntity;
import com.adminplus.pojo.entity.RoleEntity;
import com.adminplus.pojo.entity.UserRoleEntity;
import com.adminplus.repository.*;
import com.adminplus.service.DashboardService;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard 服务实现
 * <p>
 * 提供系统仪表板统计数据、图表数据、系统信息、在线用户等功能
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    // ==================== 常量定义 ====================

    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    // ==================== 依赖注入 ====================

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final LogRepository logRepository;
    private final UserRoleRepository userRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DataSource dataSource;

    @Value("${info.app.version:1.0.0}")
    private String appVersion;

    // ==================== 统计数据 ====================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboardStats", key = "'stats'")
    public DashboardStatsResponse getStats() {
        log.debug("获取 Dashboard 统计数据");

        long userCount = userRepository.countByDeletedFalse();
        long roleCount = roleRepository.countByDeletedFalse();
        long menuCount = menuRepository.countByDeletedFalse();

        // 统计今天的日志数量
        LocalDate today = LocalDate.now();
        Instant startOfToday = today.atStartOfDay(SYSTEM_ZONE).toInstant();
        Instant endOfToday = today.plusDays(1).atStartOfDay(SYSTEM_ZONE).toInstant();
        long logCount = logRepository.countByCreateTimeBetweenAndDeletedFalse(startOfToday, endOfToday);

        return new DashboardStatsResponse(userCount, roleCount, menuCount, logCount);
    }

    @Override
    @CacheEvict(value = "dashboardStats", key = "'stats'")
    public void clearStatsCache() {
        log.info("清除 Dashboard 统计数据缓存");
    }

    // ==================== 图表数据 ====================

    @Override
    @Transactional(readOnly = true)
    public ChartDataResponse getUserGrowthData() {
        log.debug("获取用户增长趋势数据");

        LocalDate today = LocalDate.now();
        List<LocalDate> dates = generateRecentDates(today, DashboardConstants.CHART_DAYS);
        List<String> dateLabels = dates.stream()
                .map(DateTimeConstants.SHORT_DATE::format)
                .collect(Collectors.toList());
        List<Long> values = new ArrayList<>();

        for (LocalDate date : dates) {
            Instant startOfDay = date.atStartOfDay(SYSTEM_ZONE).toInstant();
            Instant endOfDay = date.plusDays(1).atStartOfDay(SYSTEM_ZONE).toInstant();
            long count = userRepository.countByCreateTimeBetweenAndDeletedFalse(startOfDay, endOfDay);
            values.add(count);
        }

        log.debug("用户增长趋势数据: dates={}, values={}", dateLabels, values);
        return new ChartDataResponse(dateLabels, values);
    }

    @Override
    @Transactional(readOnly = true)
    public ChartDataResponse getRoleDistributionData() {
        log.debug("获取角色分布数据");

        // 获取所有角色
        List<RoleEntity> roles = roleRepository.findByDeletedFalse();

        // 批量查询用户角色关系，避免 N+1 问题
        List<UserRoleEntity> allUserRoles = userRoleRepository.findAll();
        Map<String, Long> roleUserCountMap = allUserRoles.stream()
                .collect(Collectors.groupingBy(UserRoleEntity::getRoleId, Collectors.counting()));

        List<String> roleNames = roles.stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toList());

        List<Long> userCounts = roles.stream()
                .map(role -> roleUserCountMap.getOrDefault(role.getId(), 0L))
                .collect(Collectors.toList());

        log.debug("角色分布数据: roles={}, counts={}", roleNames, userCounts);
        return new ChartDataResponse(roleNames, userCounts);
    }

    @Override
    @Transactional(readOnly = true)
    public ChartDataResponse getMenuDistributionData() {
        log.debug("获取菜单类型分布数据");

        List<String> types = List.of("目录", "菜单", "按钮");
        long directoryCount = menuRepository.countByTypeAndDeletedFalse(0);
        long menuCount = menuRepository.countByTypeAndDeletedFalse(1);
        long buttonCount = menuRepository.countByTypeAndDeletedFalse(2);

        List<Long> counts = List.of(directoryCount, menuCount, buttonCount);

        log.debug("菜单类型分布: 目录={}, 菜单={}, 按钮={}", directoryCount, menuCount, buttonCount);
        return new ChartDataResponse(types, counts);
    }

    @Override
    @Transactional(readOnly = true)
    public ChartDataResponse getVisitTrendData() {
        log.debug("获取访问量趋势数据");

        LocalDate today = LocalDate.now();
        List<LocalDate> dates = generateRecentDates(today, DashboardConstants.CHART_DAYS);
        List<String> dateLabels = dates.stream()
                .map(DateTimeConstants.SHORT_DATE::format)
                .collect(Collectors.toList());
        List<Long> values = new ArrayList<>();

        for (LocalDate date : dates) {
            Instant startOfDay = date.atStartOfDay(SYSTEM_ZONE).toInstant();
            Instant endOfDay = date.plusDays(1).atStartOfDay(SYSTEM_ZONE).toInstant();
            long count = logRepository.countByCreateTimeBetweenAndDeletedFalse(startOfDay, endOfDay);
            values.add(count);
        }

        log.debug("访问量趋势数据: dates={}, values={}", dateLabels, values);
        return new ChartDataResponse(dateLabels, values);
    }

    // ==================== 操作日志 ====================

    @Override
    @Transactional(readOnly = true)
    public List<OperationLogResponse> getRecentOperationLogs() {
        log.debug("获取最近操作日志");

        List<LogEntity> logs = logRepository.findTop10ByDeletedFalseOrderByCreateTimeDesc();

        return logs.stream()
                .map(log -> new OperationLogResponse(
                        log.getId(),
                        log.getUsername(),
                        log.getModule(),
                        log.getOperationType(),
                        log.getDescription(),
                        log.getIp(),
                        log.getCreateTime(),
                        log.getStatus(),
                        log.getCostTime()
                ))
                .collect(Collectors.toList());
    }

    // ==================== 系统信息 ====================

    @Override
    @Transactional(readOnly = true)
    public SystemInfoResponse getSystemInfo() {
        log.debug("获取系统信息");

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        // 计算内存使用情况（MB）
        long totalMemory = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long freeMemory = totalMemory - usedMemory;

        // 计算内存使用率（百分比）
        double memoryUsage = totalMemory > 0 ? (usedMemory * 100.0 / totalMemory) : 0.0;

        // 计算CPU使用率（JVM进程的CPU使用率）
        // com.sun.management.OperatingSystemMXBean 提供更详细的系统信息
        double cpuUsage = 0.0;
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            // getProcessCpuLoad() 返回 -1.0 如果不可用，否则返回 0.0 到 1.0 之间的值
            double processCpuLoad = sunOsBean.getProcessCpuLoad();
            if (processCpuLoad >= 0) {
                cpuUsage = processCpuLoad * 100.0;
            }
        }

        // 计算磁盘使用率
        double diskUsage = getDiskUsage();

        // 获取 JVM 运行时间（秒）
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;

        // 获取 JDK 版本
        String jdkVersion = System.getProperty("java.version");

        // 获取连接池大小
        int poolSize = dataSource instanceof HikariDataSource hikari
                ? hikari.getMaximumPoolSize()
                : DashboardConstants.DEFAULT_DB_POOL_SIZE;

        // 获取数据库版本（PostgreSQL）
        String dbVersion = getPostgreSQLVersion();

        return new SystemInfoResponse(
                "AdminPlus",
                appVersion,
                osBean.getName(),
                jdkVersion,
                totalMemory,
                usedMemory,
                freeMemory,
                memoryUsage,
                cpuUsage,
                diskUsage,
                "PostgreSQL",
                dbVersion,
                poolSize,
                uptime
        );
    }

    /**
     * 获取磁盘使用率（百分比）
     * 基于应用运行目录所在磁盘
     */
    private double getDiskUsage() {
        try {
            // 获取应用运行目录
            java.io.File root = new java.io.File("/").getAbsoluteFile();
            if (!root.exists()) {
                // Windows系统，使用当前目录所在盘符
                root = new java.io.File(".").getAbsoluteFile();
                while (root.getParentFile() != null) {
                    root = root.getParentFile();
                }
            }

            long totalSpace = root.getTotalSpace();
            long usedSpace = totalSpace - root.getFreeSpace();

            if (totalSpace > 0) {
                return (usedSpace * 100.0 / totalSpace);
            }
        } catch (Exception e) {
            log.warn("获取磁盘使用率失败", e);
        }
        return 0.0;
    }

    /**
     * 获取 PostgreSQL 数据库版本
     */
    private String getPostgreSQLVersion() {
        try {
            return dataSource.getConnection().getMetaData().getDatabaseProductVersion();
        } catch (Exception e) {
            log.warn("获取数据库版本失败", e);
            return "Unknown";
        }
    }

    // ==================== 在线用户 ====================

    @Override
    @Transactional(readOnly = true)
    public List<OnlineUserResponse> getOnlineUsers() {
        log.debug("获取在线用户列表");

        // 基于有效的 Refresh Token 获取真正的在线用户
        Instant now = Instant.now();
        List<RefreshTokenEntity> validTokens = refreshTokenRepository.findValidTokens(now);

        // 去重并获取用户信息
        Map<String, RefreshTokenEntity> latestTokenByUser = new HashMap<>();
        for (RefreshTokenEntity token : validTokens) {
            RefreshTokenEntity existing = latestTokenByUser.get(token.getUserId());
            if (existing == null || token.getExpiryDate().isAfter(existing.getExpiryDate())) {
                latestTokenByUser.put(token.getUserId(), token);
            }
        }

        // 获取用户登录信息（从最近的登录日志）
        List<OnlineUserResponse> result = new ArrayList<>();
        for (RefreshTokenEntity token : latestTokenByUser.values()) {
            userRepository.findById(token.getUserId()).ifPresent(user -> {
                // 获取该用户最近的登录日志
                List<LogEntity> userLogs = logRepository.findTop5ByUserIdAndDeletedFalseOrderByCreateTimeDesc(token.getUserId());
                LogEntity lastLoginLog = userLogs.stream()
                        .filter(log -> log.getLogType() == 2 && log.getStatus() == 1)  // 登录类型且成功
                        .findFirst()
                        .orElse(null);

                if (lastLoginLog != null) {
                    result.add(new OnlineUserResponse(
                            user.getId(),
                            user.getUsername(),
                            lastLoginLog.getIp(),
                            lastLoginLog.getCreateTime(),
                            lastLoginLog.getBrowser(),
                            lastLoginLog.getOs()
                    ));
                } else {
                    // 如果没有登录日志，使用默认值
                    result.add(new OnlineUserResponse(
                            user.getId(),
                            user.getUsername(),
                            "-",
                            token.getCreateTime(),
                            "-",
                            "-"
                    ));
                }
            });
        }

        log.debug("在线用户数量: {}", result.size());
        return result;
    }

    // ==================== 统计汇总 ====================

    @Override
    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics() {
        log.debug("获取 Statistics 页面统计数据");

        LocalDate today = LocalDate.now();
        Instant startOfToday = today.atStartOfDay(SYSTEM_ZONE).toInstant();
        Instant endOfToday = today.plusDays(1).atStartOfDay(SYSTEM_ZONE).toInstant();

        // 总用户数
        long totalUsers = userRepository.countByDeletedFalse();

        // 今日访问量
        long todayVisits = logRepository.countByCreateTimeBetweenAndDeletedFalse(startOfToday, endOfToday);

        // 活跃用户数（今日有操作的用户）
        long activeUsers = logRepository.countDistinctUsersByTimeRange(startOfToday, endOfToday);

        // 今日新增注册
        long todayNewUsers = userRepository.countByCreateTimeBetweenAndDeletedFalse(startOfToday, endOfToday);

        // 用户增长趋势数据
        ChartDataResponse userGrowthData = getUserGrowthData();

        // 访问量趋势数据
        ChartDataResponse visitTrendData = getVisitTrendData();

        return new StatisticsResponse(
                totalUsers,
                todayVisits,
                activeUsers,
                todayNewUsers,
                userGrowthData,
                visitTrendData
        );
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 生成最近 N 天的日期列表
     *
     * @param today     基准日期
     * @param days      天数
     * @return 日期列表，从最早到最近排序
     */
    private List<LocalDate> generateRecentDates(LocalDate today, int days) {
        List<LocalDate> dates = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            dates.add(today.minusDays(i));
        }
        return dates;
    }
}
