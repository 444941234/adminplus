package com.adminplus.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 2/3 兼容配置
 * <p>
 * Spring Boot 4.0 使用 Jackson 3 (tools.jackson.JsonMapper)，
 * 但项目中的很多代码仍然使用 Jackson 2 API (com.fasterxml.jackson.databind.ObjectMapper)。
 * </p>
 * <p>
 * 这里创建一个 Jackson 2 的 ObjectMapper bean，用于兼容旧代码。
 * </p>
 *
 * @author AdminPlus
 * @since 2026-04-08
 */
@Configuration
public class JacksonCompatibilityConfig {

    /**
     * 创建 Jackson 2 的 ObjectMapper bean
     * 用于兼容使用 Jackson 2 API 的代码
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册 JavaTimeModule 以支持 Java 8 日期时间类型
        mapper.registerModule(new JavaTimeModule());

        // 禁用将日期写为时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 遇到未知属性时不报错
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }
}
