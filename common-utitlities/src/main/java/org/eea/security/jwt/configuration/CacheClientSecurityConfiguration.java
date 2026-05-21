package org.eea.security.jwt.configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eea.security.jwt.data.CacheTokenVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
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
  @Value("${cache.datasetSchemaId.ttl:120}")
  private long datasetSchemaIdTtl;

  @Value("${spring.cloud.consul.discovery.instanceId}")
  private String serviceInstanceId;

  /**
   * Jedis connection factory jedis connection factory.
   *
   * @return the jedis connection factory
   */
  @Bean
  @Profile("local")
  public JedisConnectionFactory jedisConnectionFactory() {

    JedisPoolConfig poolConfig = createPoolConfig();
    RedisStandaloneConfiguration redisStandaloneConfiguration =
        new RedisStandaloneConfiguration(redisHost, redisPort);

    return new JedisConnectionFactory(redisStandaloneConfiguration, JedisClientConfiguration
        .builder().usePooling().poolConfig(poolConfig).and().clientName(serviceInstanceId).build());
  }

  /**
   * Jedis sentinel connection factory jedis connection factory.
   *
   * @return the jedis connection factory
   */
  @Bean
  @Profile("!local")
  public JedisConnectionFactory jedisSentinelConnectionFactory() {

    JedisPoolConfig poolConfig = createPoolConfig();

    RedisSentinelConfiguration redisStandaloneConfiguration =
        new RedisSentinelConfiguration(redisMasterSentinel, sentinelNodes);

    return new JedisConnectionFactory(redisStandaloneConfiguration, JedisClientConfiguration
        .builder().usePooling().poolConfig(poolConfig).and().clientName(serviceInstanceId).build());
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
    poolConfig.setTestOnBorrow(true);
    poolConfig.setTestOnReturn(true);
    poolConfig.setTestWhileIdle(true);
    return poolConfig;
  }

  /**
   * Redis Cache Manager for local profile.
   * Uses a standalone Redis connection to manage caches for
   * {@code datasetSchemaId}.
   * TTLs are configurable via Consul keys {@code cache.datasetSchemaId.ttl}
   * and {@code cache.uniqueConstraints.ttl} (in minutes).
   *
   * @param jedisConnectionFactory the standalone Redis connection factory
   * @return the Redis cache manager
   */
  @Bean
  @Profile("local")
  public RedisCacheManager localCacheManager(JedisConnectionFactory jedisConnectionFactory) {
    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                            new StringRedisSerializer()))
            .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                            new GenericJackson2JsonRedisSerializer()));

    Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
    cacheConfigs.put("datasetSchemaId", defaultConfig.entryTtl(Duration.ofMinutes(datasetSchemaIdTtl)));

    return RedisCacheManager.builder(jedisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
  }

  /**
   * Redis Cache Manager for non-local profiles (dev, staging, production).
   * Uses a Redis Sentinel connection for high availability to manage caches for
   * {@code datasetSchemaId}.
   * TTLs are configurable via Consul keys {@code cache.datasetSchemaId.ttl}
   * and {@code cache.uniqueConstraints.ttl} (in minutes).
   *
   * @param jedisSentinelConnectionFactory the Redis Sentinel connection factory
   * @return the Redis cache manager
   */
  @Bean
  @Profile("!local")
  public RedisCacheManager sentinelCacheManager(JedisConnectionFactory jedisSentinelConnectionFactory) {
    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                            new StringRedisSerializer()))
            .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                            new GenericJackson2JsonRedisSerializer()));

    Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
    cacheConfigs.put("datasetSchemaId", defaultConfig.entryTtl(Duration.ofMinutes(datasetSchemaIdTtl)));

    return RedisCacheManager.builder(jedisSentinelConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
  }

}
