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

    @Data
    public static class Jwt {
        private String secret = "";
        private String devSecret = "";
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
}
