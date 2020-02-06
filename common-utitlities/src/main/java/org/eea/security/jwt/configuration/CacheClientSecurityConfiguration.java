package org.eea.security.jwt.configuration;

import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.security.jwt.data.CacheTokenVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

  /**
   * Jedis connection factory jedis connection factory.
   *
   * @return the jedis connection factory
   */
  @Bean
  public JedisConnectionFactory jedisConnectionFactory() {

    JedisPoolConfig poolConfig = new JedisPoolConfig();
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(
        redisHost, redisPort);
    poolConfig.setMaxTotal(maxTotalRedisConnections);
    poolConfig.setMaxIdle(maxIdleRedisConnections);
    poolConfig.setMinIdle(minIdleRedisConnections);
    poolConfig.setBlockWhenExhausted(true);
    poolConfig.setMinEvictableIdleTimeMillis(minEvitableIdleTimeMillis);
    poolConfig.setMaxWaitMillis(maxWaitMillis);

    return new JedisConnectionFactory(
        redisStandaloneConfiguration,
        JedisClientConfiguration.builder().usePooling().poolConfig(poolConfig).and().build());
  }


  @Bean
  public RedisTemplate<String, CacheTokenVO> securityRedisTemplate() {
    RedisTemplate<String, CacheTokenVO> redisTemplate = new RedisTemplate<>();

    redisTemplate.setConnectionFactory(jedisConnectionFactory());
    redisTemplate.setEnableTransactionSupport(true);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(CacheTokenVO.class));
    return redisTemplate;
  }
}
