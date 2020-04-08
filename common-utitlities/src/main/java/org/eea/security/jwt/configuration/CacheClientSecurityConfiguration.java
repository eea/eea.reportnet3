package org.eea.security.jwt.configuration;

import java.util.HashSet;
import java.util.Set;
import org.eea.security.jwt.data.CacheTokenVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 * The type Cache client security configuration.
 */
@Configuration
public class CacheClientSecurityConfiguration {

  @Value("${spring.redis.host}")
  private String redisHost;
  @Value("${spring.redis.port}")
  private Integer redisPort;
  @Value("${spring.redis.sentinel.master}")
  private String redisMasterSentinel;
  @Value("${spring.redis.sentinel.nodes}")
  private Set<String> sentinelNodes;

  @Value("${spring.redis.jedis.pool.max-active}")
  private Integer maxTotalRedisConnections;
  @Value("${spring.redis.jedis.pool.max-idle}")
  private Integer maxIdleRedisConnections;
  @Value("${spring.redis.jedis.pool.min-idle}")
  private Integer minIdleRedisConnections;
  @Value("${spring.redis.jedis.pool.min-evitable-idle-time}")
  private Integer minEvitableIdleTimeMillis;
  @Value("${spring.redis.jedis.pool.max-wait}")
  private Integer maxWaitMillis;

  @Value("${spring.cloud.consul.discovery.instanceId}")
  private String serviceInstanceId;

  /**
   * Jedis connection factory jedis connection factory.
   *
   * @return the jedis connection factory
   */
  @Bean
  @Profile("!production")
  public JedisConnectionFactory jedisConnectionFactory() {

    JedisPoolConfig poolConfig = createPoolConfig();
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(
        redisHost, redisPort);

    return new JedisConnectionFactory(
        redisStandaloneConfiguration,
        JedisClientConfiguration.builder().usePooling().poolConfig(poolConfig).and()
            .clientName(serviceInstanceId).build());
  }

  /**
   * Jedis sentinel connection factory jedis connection factory.
   *
   * @return the jedis connection factory
   */
  @Bean
  @Profile("production")
  public JedisConnectionFactory jedisSentinelConnectionFactory() {

    JedisPoolConfig poolConfig = createPoolConfig();

    RedisSentinelConfiguration redisStandaloneConfiguration = new RedisSentinelConfiguration(
        redisMasterSentinel, sentinelNodes);

    return new JedisConnectionFactory(
        redisStandaloneConfiguration,
        JedisClientConfiguration.builder().usePooling().poolConfig(poolConfig).and()
            .clientName(serviceInstanceId)
            .build());
  }

  /**
   * Security redis template redis template.
   *
   * @param jedisConnectionFactory the jedis connection factory
   *
   * @return the redis template
   */
  @Bean
  public RedisTemplate<String, CacheTokenVO> securityRedisTemplate(
      JedisConnectionFactory jedisConnectionFactory) {
    RedisTemplate<String, CacheTokenVO> redisTemplate = new RedisTemplate<>();

    redisTemplate.setConnectionFactory(jedisConnectionFactory);
    redisTemplate.setEnableTransactionSupport(true);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(CacheTokenVO.class));
    return redisTemplate;
  }

  private JedisPoolConfig createPoolConfig() {
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(maxTotalRedisConnections);
    poolConfig.setMaxIdle(maxIdleRedisConnections);
    poolConfig.setMinIdle(minIdleRedisConnections);
    poolConfig.setBlockWhenExhausted(true);
    poolConfig.setMinEvictableIdleTimeMillis(minEvitableIdleTimeMillis);
    poolConfig.setMaxWaitMillis(maxWaitMillis);
    return poolConfig;
  }

}
