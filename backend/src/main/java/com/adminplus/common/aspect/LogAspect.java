package com.adminplus.common.aspect;

import com.adminplus.common.annotation.LoginLog;
import com.adminplus.common.annotation.OperationLog;
import com.adminplus.service.LogService;
import com.adminplus.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日志切面
 * 拦截带有 @OperationLog 或 @LoginLog 注解的方法，自动记录操作日志
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    private final LogService logService;
    private final ObjectMapper objectMapper;

    public LogAspect(LogService logService, ObjectMapper objectMapper) {
        this.logService = logService;
        this.objectMapper = objectMapper;
    }

    /**
     * 定义切入点：拦截所有带 @OperationLog 注解的方法
     */
    @Pointcut("@annotation(com.adminplus.common.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    /**
     * 定义切入点：拦截所有带 @LoginLog 注解的方法
     */
    @Pointcut("@annotation(com.adminplus.common.annotation.LoginLog)")
    public void loginLogPointcut() {
    }

    /**
     * 拦截 @OperationLog 注解的方法
     */
    @Around("operationLogPointcut()")
    public Object aroundOperationLog(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        // 获取请求信息
        HttpServletRequest request = getRequest();
        String ip = getClientIp(request);
        String methodName = request != null ? request.getMethod() + " " + request.getRequestURI() : signature.getName();
        String params = getParams(joinPoint);

        // 获取操作描述
        String description = parseDescription(operationLog.description(), joinPoint);

        // 执行方法
        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;

            // 记录日志
            try {
                if (error != null) {
                    // 失败日志
                    logService.log(
                            operationLog.module(),
                            operationLog.operationType(),
                            description,
                            methodName,
                            params,
                            ip
                    );
                    // 记录失败状态
                    logService.log(
                            operationLog.module(),
                            operationLog.operationType(),
                            description + " - 失败",
                            0,
                            error.getMessage()
                    );
                } else {
                    // 成功日志
                    logService.log(
                            operationLog.module(),
                            operationLog.operationType(),
                            description,
                            costTime
                    );
                }
            } catch (Exception e) {
                log.error("记录操作日志失败", e);
            }
        }
    }

    /**
     * 拦截 @LoginLog 注解的方法
     */
    @Around("loginLogPointcut()")
    public Object aroundLoginLog(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LoginLog loginLog = method.getAnnotation(LoginLog.class);

        // 获取请求信息
        HttpServletRequest request = getRequest();
        String ip = getClientIp(request);
        String methodName = request != null ? request.getMethod() + " " + request.getRequestURI() : signature.getName();
        String params = getParams(joinPoint);

        // 获取操作描述
        String description = loginLog.description();
        if (description.isEmpty()) {
            description = loginLog.type() == 1 ? "用户登录" : "用户登出";
        }

        // 获取登录用户名（从参数中获取）
        String username = getUsernameFromArgs(joinPoint.getArgs(), method);

        // 执行方法
        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;

            // 记录日志
            try {
                if (error != null) {
                    // 登录失败
                    logService.log(
                            "系统登录",
                            loginLog.type(),
                            description + " - 失败",
                            methodName,
                            params,
                            ip
                    );
                    logService.log(
                            "系统登录",
                            loginLog.type(),
                            (username != null ? username : "未知用户") + " 登录失败",
                            0,
                            error.getMessage()
                    );
                } else {
                    // 登录成功
                    logService.log(
                            "系统登录",
                            loginLog.type(),
                            description + " - 成功",
                            methodName,
                            params,
                            ip
                    );
                    logService.log(
                            "系统登录",
                            loginLog.type(),
                            (username != null ? username : "未知用户") + " " + description,
                            costTime
                    );
                }
            } catch (Exception e) {
                log.error("记录登录日志失败", e);
            }
        }
    }

    /**
     * 获取 HttpServletRequest
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果有多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取请求参数
     */
    private String getParams(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Parameter[] parameters = method.getParameters();
            Object[] args = joinPoint.getArgs();

            if (parameters.length == 0 || args.length == 0) {
                return "";
            }

            Map<String, Object> paramsMap = new HashMap<>();

            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                // 跳过 HttpServletRequest 和 HttpServletResponse
                if (param.getType().getName().contains("HttpServletRequest")
                        || param.getType().getName().contains("HttpServletResponse")) {
                    continue;
                }

                // 检查是否有 @RequestBody 注解
                RequestBody requestBody = param.getAnnotation(RequestBody.class);
                if (requestBody != null && args[i] != null) {
                    // 如果有 @RequestBody，直接序列化整个对象
                    return objectMapper.writeValueAsString(args[i]);
                }

                // 检查是否有 @RequestParam 注解
                RequestParam requestParam = param.getAnnotation(RequestParam.class);
                if (requestParam != null) {
                    String paramName = requestParam.value().isEmpty() ? param.getName() : requestParam.value();
                    paramsMap.put(paramName, args[i]);
                } else if (args[i] instanceof MultipartFile fileValue) {
                    paramsMap.put(param.getName(), Map.of(
                            "originalFilename", fileValue.getOriginalFilename(),
                            "size", fileValue.getSize(),
                            "contentType", fileValue.getContentType()
                    ));
                } else if (args[i] != null && !param.getType().getName().startsWith("java.lang")) {
                    // 对于非基本类型，尝试序列化
                    paramsMap.put(param.getName(), args[i]);
                } else {
                    paramsMap.put(param.getName(), args[i]);
                }
            }

            if (paramsMap.isEmpty()) {
                return "";
            }

            return objectMapper.writeValueAsString(paramsMap);
        } catch (Exception e) {
            log.warn("获取请求参数失败", e);
            return "";
        }
    }

    /**
     * 解析描述，支持 SpEL 表达式（简化版）
     * 目前只支持简单的 {#paramName} 格式
     */
    private String parseDescription(String description, ProceedingJoinPoint joinPoint) {
        if (description == null || description.isEmpty()) {
            return "";
        }

        // 简单的 SpEL 解析：{#paramName} -> 参数值
        try {
            if (description.contains("{#")) {
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                Method method = signature.getMethod();
                Parameter[] parameters = method.getParameters();
                Object[] args = joinPoint.getArgs();

                for (int i = 0; i < parameters.length; i++) {
                    String paramName = parameters[i].getName();
                    String placeholder = "{#" + paramName + "}";
                    if (description.contains(placeholder) && args[i] != null) {
                        description = description.replace(placeholder, String.valueOf(args[i]));
                    }
                }
            }

            // 尝试获取当前用户名
            if (description.contains("{#username}") || description.contains("{#currentUser}")) {
                try {
                    String username = SecurityUtils.getCurrentUsername();
                    description = description.replace("{#username}", username != null ? username : "");
                    description = description.replace("{#currentUser}", username != null ? username : "");
                } catch (Exception e) {
                    // 忽略安全异常
                }
            }
        } catch (Exception e) {
            log.warn("解析描述失败", e);
        }

        return description;
    }

    /**
     * 从方法参数中获取登录用户名
     */
    private String getUsernameFromArgs(Object[] args, Method method) {
        if (args == null || args.length == 0) {
            return null;
        }

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            String paramName = parameters[i].getName();
            // 常见的用户名参数名
            if ("username".equalsIgnoreCase(paramName) || "account".equalsIgnoreCase(paramName)) {
                if (args[i] != null) {
                    return String.valueOf(args[i]);
                }
            }
            // 检查是否有 @RequestBody 且类型包含 username 字段
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (requestBody != null && args[i] != null) {
                try {
                    Map<String, Object> map = objectMapper.convertValue(args[i], Map.class);
                    if (map != null) {
                        Object username = map.get("username");
                        if (username != null) {
                            return String.valueOf(username);
                        }
                        Object account = map.get("account");
                        if (account != null) {
                            return String.valueOf(account);
                        }
                    }
                } catch (Exception e) {
                    // 忽略
                }
            }
        }

        return null;
    }
}
