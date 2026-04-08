package com.adminplus.service.impl;

import com.adminplus.pojo.dto.query.LogQuery;
import com.adminplus.pojo.dto.resp.LogStatisticsResp;
import com.adminplus.service.LogStatisticsService;
import com.adminplus.service.LogStorageStrategy;
import com.adminplus.enums.LogType;
import com.adminplus.enums.LogStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志统计服务实现
 *
 * @author AdminPlus
 * @since 2026-03-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogStatisticsServiceImpl implements LogStatisticsService {

    private final LogStorageStrategySelector storageStrategySelector;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(readOnly = true)
    public LogStatisticsResp getStatistics() {
        LogStorageStrategy strategy = storageStrategySelector.getStrategy();

        // 总数统计
        long totalCount = strategy.count();

        // 按类型统计
        Map<Integer, Long> countByType = new HashMap<>();
        countByType.put(LogType.OPERATION.getCode(), countByTypeInternal(strategy, LogType.OPERATION.getCode()));
        countByType.put(LogType.LOGIN.getCode(), countByTypeInternal(strategy, LogType.LOGIN.getCode()));
        countByType.put(LogType.SYSTEM.getCode(), countByTypeInternal(strategy, LogType.SYSTEM.getCode()));

        // 按状态统计
        Map<Integer, Long> countByStatus = new HashMap<>();
        countByStatus.put(LogStatus.SUCCESS.getCode(), countByStatusInternal(strategy, LogStatus.SUCCESS.getCode()));
        countByStatus.put(LogStatus.FAILED.getCode(), countByStatusInternal(strategy, LogStatus.FAILED.getCode()));

        // 今日统计
        long todayCount = countTodayInternal(strategy);

        // 按日期统计（最近7天）
        Map<String, Long> countByDate = new HashMap<>();
        for (int i = 6; i >= 0; i--) {
            String date = getDateDaysAgo(i);
            countByDate.put(date, countByDateInternal(strategy, date));
        }

        // 按操作类型统计
        Map<Integer, Long> countByOperationType = new HashMap<>();
        for (int opType = 1; opType <= 7; opType++) {
            countByOperationType.put(opType, countByOperationTypeInternal(strategy, opType));
        }

        return new LogStatisticsResp(
                totalCount,
                countByType.get(LogType.OPERATION.getCode()),
                countByType.get(LogType.LOGIN.getCode()),
                countByType.get(LogType.SYSTEM.getCode()),
                todayCount,
                countByStatus.get(LogStatus.SUCCESS.getCode()),
                countByStatus.get(LogStatus.FAILED.getCode()),
                countByType,
                countByStatus,
                countByDate,
                countByOperationType
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LogStatisticsResp getTrendData(int days) {
        LogStorageStrategy strategy = storageStrategySelector.getStrategy();

        // 按日期统计
        Map<String, Long> countByDate = new HashMap<>();
        for (int i = days - 1; i >= 0; i--) {
            String date = getDateDaysAgo(i);
            countByDate.put(date, countByDateInternal(strategy, date));
        }

        // 计算总数
        long totalCount = countByDate.values().stream().mapToLong(Long::longValue).sum();

        return new LogStatisticsResp(
                totalCount,
                0L, 0L, 0L,  // 按类型统计
                totalCount,  // 今日统计（这里用总数代替）
                0L, 0L,      // 按状态统计
                new HashMap<>(), new HashMap<>(),
                countByDate,
                new HashMap<>() // 按操作类型统计
        );
    }

    private Long countByTypeInternal(LogStorageStrategy strategy, int logType) {
        LogQuery query = new LogQuery(1, 1, null, null, logType, null, null, null, null);
        return strategy.countByCondition(query);
    }

    private Long countByStatusInternal(LogStorageStrategy strategy, int status) {
        LogQuery query = new LogQuery(1, 1, null, null, null, null, status, null, null);
        return strategy.countByCondition(query);
    }

    private Long countTodayInternal(LogStorageStrategy strategy) {
        String todayStart = getTodayStart();
        LogQuery query = new LogQuery(1, 1, null, null, null, null, null, todayStart, null);
        return strategy.countByCondition(query);
    }

    private Long countByDateInternal(LogStorageStrategy strategy, String date) {
        String startTime = date + " 00:00:00";
        String endTime = date + " 23:59:59";
        LogQuery query = new LogQuery(1, 1, null, null, null, null, null, startTime, endTime);
        return strategy.countByCondition(query);
    }

    private Long countByOperationTypeInternal(LogStorageStrategy strategy, int operationType) {
        LogQuery query = new LogQuery(1, 1, null, null, null, operationType, null, null, null);
        return strategy.countByCondition(query);
    }

    private String getTodayStart() {
        return LocalDateTime.now()
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .format(DATE_TIME_FORMATTER);
    }

    private String getDateDaysAgo(int days) {
        return LocalDateTime.now()
                .minusDays(days)
                .format(DATE_FORMATTER);
    }
}
