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
    private Elasticsearch elasticsearch = new Elasticsearch();

    @Data
    public static class Jwt {
        private String secret = "";
        private String devSecret = "";
        private int accessTokenExpirationHours = 2;
        private int refreshTokenExpirationDays = 7;
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
        private int loginMaxRequests = 5;
        private int loginTimeWindow = 60;
        private int generalMaxRequests = 100;
        private int generalTimeWindow = 60;
    }

    @Data
    public static class WorkflowHook {
        private boolean allowInternalUrls = false;
        private String allowedUrlPatterns = "";
    }

    @Data
    public static class Elasticsearch {
        private boolean enabled = false;
        private String urls = "http://localhost:9200";
        private String username = "";
        private String password = "";
        private int connectionTimeout = 10;
        private int socketTimeout = 30;
    }
}