package com.adminplus.common.aspect;

import com.adminplus.common.annotation.LoginLog;
import com.adminplus.common.annotation.OperationLog;
import com.adminplus.service.LogService;
import com.adminplus.utils.IpUtils;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.utils.WebUtils;
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
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
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

    @Pointcut("@annotation(com.adminplus.common.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    @Pointcut("@annotation(com.adminplus.common.annotation.LoginLog)")
    public void loginLogPointcut() {
    }

    @Around("operationLogPointcut()")
    public Object aroundOperationLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        return executeWithLogging(joinPoint, new OperationLogContext(
            operationLog.module(),
            operationLog.operationType(),
            parseDescription(operationLog.description(), joinPoint)
        ));
    }

    @Around("loginLogPointcut()")
    public Object aroundLoginLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LoginLog loginLog = method.getAnnotation(LoginLog.class);

        String description = loginLog.description();
        if (description.isEmpty()) {
            description = loginLog.type() == 1 ? "用户登录" : "用户登出";
        }
        String username = getUsernameFromArgs(joinPoint.getArgs(), method);

        return executeWithLogging(joinPoint, new LoginLogContext(
            loginLog.type(),
            description,
            username
        ));
    }

    /**
     * 日志执行模板方法
     */
    private Object executeWithLogging(ProceedingJoinPoint joinPoint, LogContext context) throws Throwable {
        long startTime = System.currentTimeMillis();

        HttpServletRequest request = getRequest();
        String ip = getClientIp(request);
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = request != null ? request.getMethod() + " " + request.getRequestURI() : signature.getName();
        String params = getParams(joinPoint);

        Throwable error = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            context.recordLog(logService, methodName, params, ip, costTime, error);
        }
    }

    /**
     * 日志上下文基类
     */
    private sealed abstract class LogContext permits OperationLogContext, LoginLogContext {
        protected final String module;
        protected final int operationType;
        protected final String description;

        protected LogContext(String module, int operationType, String description) {
            this.module = module;
            this.operationType = operationType;
            this.description = description;
        }

        protected abstract void recordLog(LogService logService, String methodName, String params,
                                         String ip, long costTime, Throwable error);
    }

    /**
     * 操作日志上下文
     */
    private final class OperationLogContext extends LogContext {
        private OperationLogContext(String module, int operationType, String description) {
            super(module, operationType, description);
        }

        @Override
        protected void recordLog(LogService logService, String methodName, String params,
                                String ip, long costTime, Throwable error) {
            try {
                if (error != null) {
                    logService.log(module, operationType, description, methodName, params, ip);
                    logService.log(module, operationType, description + " - 失败", 0, error.getMessage());
                } else {
                    logService.log(module, operationType, description, costTime);
                }
            } catch (Exception e) {
                log.error("记录操作日志失败", e);
            }
        }
    }

    /**
     * 登录日志上下文
     */
    private final class LoginLogContext extends LogContext {
        private final String username;

        private LoginLogContext(int operationType, String description, String username) {
            super("系统登录", operationType, description);
            this.username = username;
        }

        @Override
        protected void recordLog(LogService logService, String methodName, String params,
                                String ip, long costTime, Throwable error) {
            try {
                // 使用 logLogin 方法记录登录日志，不受认证状态限制
                if (error != null) {
                    logService.logLogin(username != null ? username : "未知用户", 0, error.getMessage());
                } else {
                    logService.logLogin(username != null ? username : "未知用户", 1, null);
                }
            } catch (Exception e) {
                log.error("记录登录日志失败", e);
            }
        }
    }

    private HttpServletRequest getRequest() {
        return WebUtils.getRequest();
    }

    private String getClientIp(HttpServletRequest request) {
        return IpUtils.getClientIp(request);
    }

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
                if (param.getType().getName().contains("HttpServletRequest")
                        || param.getType().getName().contains("HttpServletResponse")) {
                    continue;
                }

                RequestBody requestBody = param.getAnnotation(RequestBody.class);
                if (requestBody != null && args[i] != null) {
                    return objectMapper.writeValueAsString(args[i]);
                }

                RequestParam requestParam = param.getAnnotation(RequestParam.class);
                if (requestParam != null) {
                    String paramName = requestParam.value().isEmpty() ? param.getName() : requestParam.value();
                    // 处理 MultipartFile，避免序列化失败
                    if (args[i] instanceof MultipartFile fileValue) {
                        paramsMap.put(paramName, Map.of(
                                "originalFilename", fileValue.getOriginalFilename(),
                                "size", fileValue.getSize(),
                                "contentType", fileValue.getContentType()
                        ));
                    } else {
                        paramsMap.put(paramName, args[i]);
                    }
                } else if (args[i] instanceof MultipartFile fileValue) {
                    paramsMap.put(param.getName(), Map.of(
                            "originalFilename", fileValue.getOriginalFilename(),
                            "size", fileValue.getSize(),
                            "contentType", fileValue.getContentType()
                    ));
                } else if (args[i] != null && !param.getType().getName().startsWith("java.lang")) {
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

    private String parseDescription(String description, ProceedingJoinPoint joinPoint) {
        if (description == null || description.isEmpty()) {
            return "";
        }

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

    private String getUsernameFromArgs(Object[] args, Method method) {
        if (args == null || args.length == 0) {
            return null;
        }

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            String paramName = parameters[i].getName();
            if ("username".equalsIgnoreCase(paramName) || "account".equalsIgnoreCase(paramName)) {
                if (args[i] != null) {
                    return String.valueOf(args[i]);
                }
            }
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
