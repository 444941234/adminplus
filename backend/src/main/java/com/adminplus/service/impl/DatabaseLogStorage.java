package com.adminplus.service.impl;

import com.adminplus.constants.DateTimeConstants;
import com.adminplus.pojo.dto.query.LogQuery;
import com.adminplus.pojo.dto.response.LogPageResponse;
import com.adminplus.pojo.dto.response.PageResultResponse;
import com.adminplus.pojo.entity.LogEntity;
import com.adminplus.repository.LogRepository;
import com.adminplus.service.LogStorageStrategy;
import com.adminplus.utils.PageUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库日志存储策略实现
 *
 * @author AdminPlus
 * @since 2026-03-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseLogStorage implements LogStorageStrategy {

    private final LogRepository logRepository;

    @Override
    @Transactional
    public LogEntity save(LogEntity log) {
        return logRepository.save(log);
    }

    @Override
    @Transactional
    public List<LogEntity> saveAll(List<LogEntity> logs) {
        return logRepository.saveAll(logs);
    }

    @Override
    @Transactional(readOnly = true)
    public LogEntity findById(String id) {
        return logRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResultResponse<LogPageResponse> findPage(LogQuery query) {
        var pageable = PageUtils.toPageableDesc(query.getPage(), query.getSize(), "createTime");

        Specification<LogEntity> spec = buildSpecification(query);
        Page<LogEntity> pageResult = logRepository.findAll(spec, pageable);

        return PageResultResponse.from(pageResult, this::toLogPageVO);
    }

    @Override
    @Transactional(readOnly = true)
    public Long count() {
        return logRepository.countByDeletedFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByCondition(LogQuery query) {
        Specification<LogEntity> spec = buildSpecification(query);
        return logRepository.count(spec);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        logRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Integer deleteByIds(List<String> ids) {
        List<LogEntity> logs = logRepository.findAllById(ids);
        logRepository.deleteAll(logs);
        return logs.size();
    }

    @Override
    @Transactional
    public Integer deleteByCondition(LogQuery query) {
        Specification<LogEntity> spec = buildSpecification(query);
        List<LogEntity> logs = logRepository.findAll(spec);
        logRepository.deleteAll(logs);
        return logs.size();
    }

    @Override
    @Transactional
    public Integer cleanupExpiredLogs(int retentionDays, int batchSize) {
        Instant expireTime = Instant.now().minusSeconds(retentionDays * 24L * 60 * 60);

        int totalDeleted = 0;
        List<LogEntity> batch;

        do {
            // 分批查询过期日志
            var pageable = PageRequest.of(0, batchSize, Sort.by(Sort.Direction.ASC, "createTime"));
            Specification<LogEntity> spec = (root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.and(
                            criteriaBuilder.lessThan(root.get("createTime"), expireTime),
                            criteriaBuilder.equal(root.get("deleted"), false)
                    );

            batch = logRepository.findAll(spec, pageable).getContent();

            if (!batch.isEmpty()) {
                logRepository.deleteAll(batch);
                totalDeleted += batch.size();
                log.info("清理过期日志批次完成，本批次删除 {} 条", batch.size());
            }

        } while (batch.size() == batchSize);

        log.info("清理过期日志完成，共删除 {} 条", totalDeleted);
        return totalDeleted;
    }

    @Override
    public String getStrategyName() {
        return "DATABASE";
    }

    @Override
    public boolean isAvailable() {
        return true; // 数据库总是可用的
    }

    /**
     * 构建查询条件
     */
    private Specification<LogEntity> buildSpecification(LogQuery query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 日志类型
            if (query.getLogType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("logType"), query.getLogType()));
            }

            // 用户名模糊查询
            if (query.getUsername() != null && !query.getUsername().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("username"), "%" + query.getUsername() + "%"));
            }

            // 模块精确查询
            if (query.getModule() != null && !query.getModule().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("module"), query.getModule()));
            }

            // 操作类型精确查询
            if (query.getOperationType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("operationType"), query.getOperationType()));
            }

            // 状态精确查询
            if (query.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), query.getStatus()));
            }

            // 时间范围查询
            if (query.getStartTime() != null && !query.getStartTime().isEmpty()) {
                LocalDateTime startDateTime = LocalDateTime.parse(query.getStartTime(), DateTimeConstants.STANDARD_DATE_TIME);
                Instant startInstant = startDateTime.atZone(ZoneOffset.UTC).toInstant();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), startInstant));
            }

            if (query.getEndTime() != null && !query.getEndTime().isEmpty()) {
                LocalDateTime endDateTime = LocalDateTime.parse(query.getEndTime(), DateTimeConstants.STANDARD_DATE_TIME);
                Instant endInstant = endDateTime.atZone(ZoneOffset.UTC).toInstant();
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createTime"), endInstant));
            }

            // 默认只查询未删除的
            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 转换为 VO
     */
    private LogPageResponse toLogPageVO(LogEntity entity) {
        return new LogPageResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getModule(),
                entity.getLogType(),
                entity.getOperationType(),
                entity.getDescription(),
                entity.getMethod(),
                entity.getParams(),
                entity.getIp(),
                entity.getLocation(),
                entity.getCostTime(),
                entity.getStatus(),
                entity.getErrorMsg(),
                entity.getCreateTime()
        );
    }
}
