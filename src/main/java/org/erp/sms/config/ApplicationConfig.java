package org.erp.sms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class ApplicationConfig {
    private String name;
    private String baseUrl;
    private String emailFrom;
    private Security security = new Security();
    private Pagination pagination = new Pagination();

    @Getter
    @Setter
    public static class Security {
        private int maxLoginAttempts;
        private int lockoutDurationMinutes;
    }

    @Getter
    @Setter
    public static class Pagination {
        private int defaultPageSize;
        private int maxPageSize;
    }
}
