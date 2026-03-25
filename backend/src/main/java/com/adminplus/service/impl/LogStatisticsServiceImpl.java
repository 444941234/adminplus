package com.adminplus.service.impl;

import com.adminplus.pojo.dto.req.LogQueryReq;
import com.adminplus.pojo.dto.resp.LogStatisticsResp;
import com.adminplus.service.LogStatisticsService;
import com.adminplus.service.LogStorageStrategy;
import com.adminplus.constants.LogType;
import com.adminplus.constants.LogStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    public LogStatisticsResp getStatistics() {
        LogStorageStrategy strategy = storageStrategySelector.getStrategy();

        // 总数统计
        long totalCount = strategy.count();

        // 按类型统计
        Map<Integer, Long> countByType = new HashMap<>();
        countByType.put(LogType.OPERATION, countByTypeInternal(strategy, LogType.OPERATION));
        countByType.put(LogType.LOGIN, countByTypeInternal(strategy, LogType.LOGIN));
        countByType.put(LogType.SYSTEM, countByTypeInternal(strategy, LogType.SYSTEM));

        // 按状态统计
        Map<Integer, Long> countByStatus = new HashMap<>();
        countByStatus.put(LogStatus.SUCCESS, countByStatusInternal(strategy, LogStatus.SUCCESS));
        countByStatus.put(LogStatus.FAILED, countByStatusInternal(strategy, LogStatus.FAILED));

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
                countByType.get(LogType.OPERATION),
                countByType.get(LogType.LOGIN),
                countByType.get(LogType.SYSTEM),
                todayCount,
                countByStatus.get(LogStatus.SUCCESS),
                countByStatus.get(LogStatus.FAILED),
                countByType,
                countByStatus,
                countByDate,
                countByOperationType
        );
    }

    @Override
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
        LogQueryReq query = new LogQueryReq();
        query.setLogType(logType);
        query.setPage(1);
        query.setSize(1);
        return strategy.countByCondition(query);
    }

    private Long countByStatusInternal(LogStorageStrategy strategy, int status) {
        LogQueryReq query = new LogQueryReq();
        query.setStatus(status);
        query.setPage(1);
        query.setSize(1);
        return strategy.countByCondition(query);
    }

    private Long countTodayInternal(LogStorageStrategy strategy) {
        String todayStart = getTodayStart();
        LogQueryReq query = new LogQueryReq();
        query.setStartTime(todayStart);
        query.setPage(1);
        query.setSize(1);
        return strategy.countByCondition(query);
    }

    private Long countByDateInternal(LogStorageStrategy strategy, String date) {
        String startTime = date + " 00:00:00";
        String endTime = date + " 23:59:59";
        LogQueryReq query = new LogQueryReq();
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setPage(1);
        query.setSize(1);
        return strategy.countByCondition(query);
    }

    private Long countByOperationTypeInternal(LogStorageStrategy strategy, int operationType) {
        LogQueryReq query = new LogQueryReq();
        query.setOperationType(operationType);
        query.setPage(1);
        query.setSize(1);
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
