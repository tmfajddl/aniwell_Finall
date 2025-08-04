package com.example.RSW.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // 공유 Redis (팀용)
    private static final String SHARED_HOST = "100.65.187.38"; // 공유 IP (Tailscale)
    private static final int SHARED_PORT = 6379;

    // 개인 Redis (혼자 테스트용)
    private static final String PERSONAL_HOST = "100.114.185.63"; // 개인 Redis IP
    private static final int PERSONAL_PORT = 6379;

    private static final String PASSWORD = "aniwell1234";

    // ✅ true면 개인 Redis, false면 공유 Redis
    private boolean usePersonalRedis = false;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        String host = usePersonalRedis ? PERSONAL_HOST : SHARED_HOST;
        int port = usePersonalRedis ? PERSONAL_PORT : SHARED_PORT;

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        config.setPassword(PASSWORD);

        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // JSON 직렬화
        return template;
    }

}
