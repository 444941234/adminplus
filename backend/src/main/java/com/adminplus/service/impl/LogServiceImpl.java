package com.adminplus.service.impl;

import com.adminplus.common.exception.BizException;
import com.adminplus.constants.LogStatus;
import com.adminplus.pojo.dto.req.LogQueryDTO;
import com.adminplus.pojo.dto.resp.LogPageVO;
import com.adminplus.pojo.dto.resp.PageResultResp;
import com.adminplus.pojo.entity.LogEntity;
import com.adminplus.repository.LogRepository;
import com.adminplus.service.LogService;
import com.adminplus.utils.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 日志服务实现
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LogRepository logRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Async
    @Transactional
    public void log(String module, Integer operationType, String description) {
        log(module, operationType, description, null, null, null);
    }

    @Override
    @Async
    @Transactional
    public void log(String module, Integer operationType, String description,
                    String method, String params, String ip) {
        try {
            // 检查 SecurityContext 是否包含用户信息，如果为空则跳过日志保存
            if (!SecurityUtils.isAuthenticated()) {
                log.debug("用户已登出，跳过日志保存: module={}, operationType={}, description={}", module, operationType, description);
                return;
            }

            LogEntity logEntity = new LogEntity();
            logEntity.setUserId(SecurityUtils.getCurrentUserId());
            logEntity.setUsername(SecurityUtils.getCurrentUsername());
            logEntity.setModule(module);
            logEntity.setOperationType(operationType);
            logEntity.setDescription(description);
            logEntity.setMethod(method);
            logEntity.setParams(params);
            logEntity.setIp(ip);
            logEntity.setStatus(LogStatus.SUCCESS);
            logEntity.setCostTime(0L);

            logRepository.save(logEntity);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    @Override
    @Async
    @Transactional
    public void log(String module, Integer operationType, String description, Long costTime) {
        try {
            // 检查 SecurityContext 是否包含用户信息，如果为空则跳过日志保存
            if (!SecurityUtils.isAuthenticated()) {
                log.debug("用户已登出，跳过日志保存: module={}, operationType={}, description={}", module, operationType, description);
                return;
            }

            LogEntity logEntity = new LogEntity();
            logEntity.setUserId(SecurityUtils.getCurrentUserId());
            logEntity.setUsername(SecurityUtils.getCurrentUsername());
            logEntity.setModule(module);
            logEntity.setOperationType(operationType);
            logEntity.setDescription(description);
            logEntity.setCostTime(costTime);
            logEntity.setStatus(LogStatus.SUCCESS);

            logRepository.save(logEntity);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    @Override
    @Async
    @Transactional
    public void log(String module, Integer operationType, String description,
                    Integer status, String errorMsg) {
        try {
            // 检查 SecurityContext 是否包含用户信息，如果为空则跳过日志保存
            if (!SecurityUtils.isAuthenticated()) {
                log.debug("用户已登出，跳过日志保存: module={}, operationType={}, description={}", module, operationType, description);
                return;
            }

            LogEntity logEntity = new LogEntity();
            logEntity.setUserId(SecurityUtils.getCurrentUserId());
            logEntity.setUsername(SecurityUtils.getCurrentUsername());
            logEntity.setModule(module);
            logEntity.setOperationType(operationType);
            logEntity.setDescription(description);
            logEntity.setStatus(status);
            logEntity.setErrorMsg(errorMsg);

            logRepository.save(logEntity);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResultResp<LogPageVO> findPage(LogQueryDTO query) {
        var pageable = PageRequest.of(query.getPage() - 1, query.getSize(), Sort.by(Sort.Direction.DESC, "createTime"));

        Specification<LogEntity> spec = buildSpecification(query);
        Page<LogEntity> pageResult = logRepository.findAll(spec, pageable);

        List<LogPageVO> records = pageResult.getContent().stream()
                .map(this::toLogPageVO)
                .toList();

        return new PageResultResp<>(
                records,
                pageResult.getTotalElements(),
                pageResult.getNumber() + 1,
                pageResult.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LogPageVO findById(String id) {
        var logEntity = logRepository.findById(id)
                .orElseThrow(() -> new BizException("日志不存在"));
        return toLogPageVO(logEntity);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        if (!logRepository.existsById(id)) {
            throw new BizException("日志不存在");
        }
        logRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByIds(List<String> ids) {
        List<LogEntity> logs = logRepository.findAllById(ids);
        if (logs.isEmpty()) {
            throw new BizException("日志不存在");
        }
        logRepository.deleteAll(logs);
    }

    /**
     * 构建查询条件
     */
    private Specification<LogEntity> buildSpecification(LogQueryDTO query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

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
                LocalDateTime startDateTime = LocalDateTime.parse(query.getStartTime(), DATE_TIME_FORMATTER);
                Instant startInstant = startDateTime.atZone(ZoneOffset.UTC).toInstant();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), startInstant));
            }

            if (query.getEndTime() != null && !query.getEndTime().isEmpty()) {
                LocalDateTime endDateTime = LocalDateTime.parse(query.getEndTime(), DATE_TIME_FORMATTER);
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
    private LogPageVO toLogPageVO(LogEntity entity) {
        return new LogPageVO(
                entity.getId(),
                entity.getUsername(),
                entity.getModule(),
                entity.getOperationType(),
                entity.getDescription(),
                entity.getIp(),
                entity.getLocation(),
                entity.getCostTime(),
                entity.getStatus(),
                entity.getCreateTime()
        );
    }
}