package com.sean.synovision.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author sean
 * @Date 2025/43/18
 * 主要实现序列化
 */
@Configuration
public class RedisConfig {


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        //设置相应 key 的序列化
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //value 序列化
        //redis 默认是 jdk 的序列化是二进制,这里使用的是通用的json 数据,不用传具体的序列化的对象
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        //设置相应的 hash 序列化
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        //注入连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
}