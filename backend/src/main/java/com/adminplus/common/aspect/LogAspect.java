package com.adminplus.common.aspect;

import com.adminplus.common.annotation.LoginLog;
import com.adminplus.common.annotation.OperationLog;
import com.adminplus.pojo.dto.req.LogEntry;
import com.adminplus.service.LogService;
import com.adminplus.utils.IpUtils;
import com.adminplus.utils.SecurityUtils;
import com.adminplus.utils.WebUtils;
import tools.jackson.databind.json.JsonMapper;
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
 * 拦截带有 @OperationLog 或 @LoginLog 注解的方法，自动记录日志
 *
 * @author AdminPlus
 * @since 2026-03-03
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    private final LogService logService;
    private final JsonMapper jsonMapper;

    public LogAspect(LogService logService, JsonMapper jsonMapper) {
        this.logService = logService;
        this.jsonMapper = jsonMapper;
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

        HttpServletRequest request = WebUtils.getRequest();
        String ip = IpUtils.getClientIp(request);
        String methodName = request != null
            ? request.getMethod() + " " + request.getRequestURI()
            : signature.getName();
        String params = getParams(joinPoint);
        String description = parseDescription(operationLog.description(), joinPoint);

        long startTime = System.currentTimeMillis();
        Throwable error = null;

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;

            if (error != null) {
                // 失败日志
                logService.log(LogEntry.operationBuilder(operationLog.module(), operationLog.operationType(), description + " - 失败")
                    .method(methodName)
                    .params(params)
                    .ip(ip)
                    .failed(error.getMessage())
                    .build());
            } else {
                // 成功日志
                logService.log(LogEntry.operationBuilder(operationLog.module(), operationLog.operationType(), description)
                    .method(methodName)
                    .params(params)
                    .ip(ip)
                    .costTime(costTime)
                    .build());
            }
        }
    }

    @Around("loginLogPointcut()")
    public Object aroundLoginLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LoginLog loginLog = method.getAnnotation(LoginLog.class);

        String username = getUsernameFromArgs(joinPoint.getArgs(), method);
        String description = loginLog.description().isEmpty()
            ? (loginLog.type() == 1 ? "用户登录" : "用户登出")
            : loginLog.description();

        Throwable error = null;

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            boolean success = error == null;
            String errorMsg = success ? null : error.getMessage();

            // 登录日志（无需认证检查）
            logService.log(LogEntry.login(username != null ? username : "未知用户", success, errorMsg));
        }
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
                    return jsonMapper.writeValueAsString(args[i]);
                }

                RequestParam requestParam = param.getAnnotation(RequestParam.class);
                if (requestParam != null) {
                    String paramName = requestParam.value().isEmpty() ? param.getName() : requestParam.value();
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
                } else {
                    paramsMap.put(param.getName(), args[i]);
                }
            }

            if (paramsMap.isEmpty()) {
                return "";
            }

            return jsonMapper.writeValueAsString(paramsMap);
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
                    Map<String, Object> map = jsonMapper.convertValue(args[i], Map.class);
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