package com.adminplus.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String env = "dev";
    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Virus virus = new Virus();
    private RateLimit rateLimit = new RateLimit();
    private WorkflowHook workflowHook = new WorkflowHook();

    @Data
    public static class Jwt {
        private String secret = "";
        private String devSecret = "";
        private int expirationHours = 2;  // JWT token expiration time in hours
    }

    @Data
    public static class Cors {
        private String allowedOrigins = "http://localhost:5173,http://localhost:3000";
    }

    @Data
    public static class Virus {
        private Scan scan = new Scan();

        @Data
        public static class Scan {
            private boolean enabled = true;
            private Clamav clamav = new Clamav();
            private int timeout = 30000;

            @Data
            public static class Clamav {
                private String host = "localhost";
                private int port = 3310;
            }
        }
    }

    @Data
    public static class RateLimit {
        private int loginMaxRequests = 5;      // 登录接口最大请求数/分钟
        private int loginTimeWindow = 60;      // 登录限流时间窗口（秒）
        private int generalMaxRequests = 100;  // 通用接口最大请求数/分钟
        private int generalTimeWindow = 60;    // 通用限流时间窗口（秒）
    }

    @Data
    public static class WorkflowHook {
        private boolean allowInternalUrls = false;  // 是否允许内网URL（默认禁用以防止SSRF）
        private String allowedUrlPatterns = "";     // 允许的URL模式（逗号分隔，如：https://api.example.com/*,https://hooks.example.com/*）
    }
}
