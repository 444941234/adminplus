package com.adminplus.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Configuration Cache Configuration
 *
 * Cache strategy for configuration management:
 * - ConfigGroup: 30 minutes TTL
 * - Config: 10 minutes TTL (faster invalidation for changes)
 * - ConfigByKey: 5 minutes TTL (hot data)
 *
 * @author AdminPlus
 */
@Configuration
@EnableCaching
public class ConfigCacheConfig {

    private static final String CONFIG_GROUP_CACHE = "configGroups";
    private static final String CONFIG_CACHE = "configs";
    private static final String CONFIG_BY_KEY_CACHE = "configByKey";

    @Bean
    public CacheManager configCacheManager(RedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<Object> jsonSerializer = createJsonSerializer();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // ConfigGroup cache - 30 minutes
        RedisCacheConfiguration configGroupCache = defaultConfig.entryTtl(Duration.ofMinutes(30));

        // Config cache - 10 minutes
        RedisCacheConfiguration configCache = defaultConfig.entryTtl(Duration.ofMinutes(10));

        // Config by key cache - 5 minutes (hot data)
        RedisCacheConfiguration configByKeyCache = defaultConfig.entryTtl(Duration.ofMinutes(5));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration(CONFIG_GROUP_CACHE, configGroupCache)
                .withCacheConfiguration(CONFIG_CACHE, configCache)
                .withCacheConfiguration(CONFIG_BY_KEY_CACHE, configByKeyCache)
                .transactionAware()
                .build();
    }

    private Jackson2JsonRedisSerializer<Object> createJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
    }
}
