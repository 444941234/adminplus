package com.adminplus.common.aspect;

import com.adminplus.common.annotation.LoginLog;
import com.adminplus.common.annotation.OperationLog;
import com.adminplus.enums.OperationType;
import com.adminplus.pojo.dto.request.LogEntry;
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
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
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
 * <p>支持两种场景：</p>
 * <ul>
 *   <li>Controller 层：记录 IP、请求参数、执行时间</li>
 *   <li>Service 层：仅记录模块、操作类型、描述</li>
 * </ul>
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
    private final SpelExpressionParser spelParser = new SpelExpressionParser();

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

        // 获取操作类型代码
        int opCode = getOperationCode(operationLog);

        // 判断是否为 Controller 层调用（通过是否有 HttpServletRequest）
        HttpServletRequest request = WebUtils.getRequest();
        boolean isControllerCall = request != null;

        if (isControllerCall) {
            // Controller 层：记录完整信息
            return handleControllerLog(joinPoint, operationLog, opCode, request);
        } else {
            // Service 层：仅记录核心信息
            return handleServiceLog(joinPoint, operationLog, opCode);
        }
    }

    /**
     * 处理 Controller 层日志（记录 IP、参数、执行时间）
     */
    private Object handleControllerLog(ProceedingJoinPoint joinPoint, OperationLog operationLog,
                                        int opCode, HttpServletRequest request) throws Throwable {
        String ip = IpUtils.getClientIp(request);
        String methodName = request.getMethod() + " " + request.getRequestURI();
        String params = getParams(joinPoint);
        String description = parseControllerDescription(operationLog.description(), joinPoint);

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
                logService.log(LogEntry.operationBuilder(operationLog.module(), opCode, description + " - 失败")
                    .method(methodName)
                    .params(params)
                    .ip(ip)
                    .failed(error.getMessage())
                    .build());
            } else {
                logService.log(LogEntry.operationBuilder(operationLog.module(), opCode, description)
                    .method(methodName)
                    .params(params)
                    .ip(ip)
                    .costTime(costTime)
                    .build());
            }
        }
    }

    /**
     * 处理 Service 层日志（仅记录核心信息）
     */
    private Object handleServiceLog(ProceedingJoinPoint joinPoint, OperationLog operationLog,
                                     int opCode) throws Throwable {
        Throwable error = null;
        Object result = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            String description = parseServiceDescription(
                operationLog.description(),
                joinPoint,
                error == null && operationLog.includeResult() ? result : null
            );

            if (error != null) {
                logService.log(LogEntry.operation(operationLog.module(), opCode,
                    description + " [失败: " + error.getMessage() + "]"));
            } else {
                logService.log(LogEntry.operation(operationLog.module(), opCode, description));
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
            logService.log(LogEntry.login(username != null ? username : "未知用户", success, errorMsg));
        }
    }

    /**
     * 获取操作类型代码
     */
    private int getOperationCode(OperationLog operationLog) {
        // 优先使用 type() 属性（枚举）
        OperationType type = operationLog.type();
        if (type != OperationType.OTHER) {
            return type.getCode();
        }
        // 兼容旧代码的 operationType() 属性
        return operationLog.operationType();
    }

    /**
     * 解析 Controller 层描述（使用 {#paramName} 语法）
     */
    private String parseControllerDescription(String description, ProceedingJoinPoint joinPoint) {
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

    /**
     * 解析 Service 层描述（使用标准 SpEL 语法）
     */
    private String parseServiceDescription(String template, ProceedingJoinPoint joinPoint, Object result) {
        if (template == null || template.isEmpty()) {
            return "";
        }

        if (!template.contains("#")) {
            return template;
        }

        StandardEvaluationContext context = new StandardEvaluationContext();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            context.setVariable(parameters[i].getName(), args[i]);
        }

        if (result != null) {
            context.setVariable("result", result);
        }

        try {
            return spelParser.parseExpression(template, new TemplateParserContext())
                .getValue(context, String.class);
        } catch (Exception e) {
            log.warn("解析 SpEL 表达式失败: {}, 模板: {}", e.getMessage(), template);
            return template;
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