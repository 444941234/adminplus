package com.adminplus.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * 测试环境 Jackson 工具类
 *
 * 提供配置好的 ObjectMapper，支持 Optional 和 Java 8 时间类型。
 * Spring Boot 4 默认使用 Jackson 3，但测试中 MockMvc 可能使用 Jackson 2。
 *
 * @author AdminPlus
 * @since 2026-04-08
 */
public final class TestJacksonConfig {

    private TestJacksonConfig() {
        // Utility class
    }

    /**
     * 创建配置好的 ObjectMapper
     *
     * @return 配置了 Jdk8Module 和 JavaTimeModule 的 ObjectMapper
     */
    public static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}