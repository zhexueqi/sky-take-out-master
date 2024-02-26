package com.sky.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author zhexueqi
 * @ClassName RedisConfiguration
 * @since 2024/2/26    16:31
 */
@Configuration
@Slf4j
public class RedisConfiguration {


    @Bean //RedisConnectionFactory 在我们引入的redis依赖中已经为我们生成并放入了ioc容器中，所以这里只用声明
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        //初始化redisTemplate对象
        RedisTemplate redisTemplate = new RedisTemplate();
        //设置redis的连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        //设置redis key的序列化器
        redisTemplate.setStringSerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
