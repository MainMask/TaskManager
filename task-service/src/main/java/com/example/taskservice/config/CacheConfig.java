package com.example.taskservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    // Redis CacheManager — active only when app.cache.redis.enabled=true AND Lettuce is on the classpath
    @Bean
    @SuppressWarnings("removal") // Jackson2JsonRedisSerializer deprecated; no stable replacement yet in Spring Data Redis 4.x
    @ConditionalOnProperty(name = "app.cache.redis.enabled", havingValue = "true", matchIfMissing = false)
    @ConditionalOnClass(name = "io.lettuce.core.RedisClient")
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        var serializer = new Jackson2JsonRedisSerializer<>(buildObjectMapper(), Object.class);
        var config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer));
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    // Redis connection factory — same conditions as the cache manager
    @Bean
    @ConditionalOnProperty(name = "app.cache.redis.enabled", havingValue = "true", matchIfMissing = false)
    @ConditionalOnClass(name = "io.lettuce.core.RedisClient")
    public RedisConnectionFactory redisConnectionFactory(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port) {
        return new LettuceConnectionFactory(host, port);
    }

    // In-memory fallback — used when no RedisCacheManager is registered (e.g., tests or Redis disabled)
    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager simpleCacheManager() {
        var manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                new ConcurrentMapCache("tasks"),
                new ConcurrentMapCache("users"),
                new ConcurrentMapCache("categories")
        ));
        return manager;
    }

    private ObjectMapper buildObjectMapper() {
        var om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.activateDefaultTypingAsProperty(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                "@class");
        return om;
    }
}
