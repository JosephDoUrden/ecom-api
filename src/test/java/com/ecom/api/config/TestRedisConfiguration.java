package com.ecom.api.config;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestRedisConfiguration {

  private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7-alpine");
  private static final int REDIS_PORT = 6379;

  static {
    GenericContainer<?> redis = new GenericContainer<>(REDIS_IMAGE)
        .withExposedPorts(REDIS_PORT);
    redis.start();

    System.setProperty("spring.data.redis.host", redis.getHost());
    System.setProperty("spring.data.redis.port", redis.getMappedPort(REDIS_PORT).toString());
  }

  @Bean
  @Primary
  public RedisProperties redisProperties() {
    RedisProperties redisProperties = new RedisProperties();
    redisProperties.setHost(System.getProperty("spring.data.redis.host"));
    redisProperties.setPort(Integer.parseInt(System.getProperty("spring.data.redis.port")));
    return redisProperties;
  }

  @Bean
  public JedisConnectionFactory testRedisConnectionFactory(RedisProperties redisProperties) {
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(
        redisProperties.getHost(),
        redisProperties.getPort());
    return new JedisConnectionFactory(config);
  }

  @Bean
  public RedisTemplate<String, Object> testRedisTemplate(JedisConnectionFactory testRedisConnectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(testRedisConnectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new JdkSerializationRedisSerializer());
    template.setHashValueSerializer(new JdkSerializationRedisSerializer());
    template.afterPropertiesSet();
    return template;
  }
}
