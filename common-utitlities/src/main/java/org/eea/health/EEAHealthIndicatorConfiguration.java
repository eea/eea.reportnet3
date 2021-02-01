package org.eea.health;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.sql.DataSource;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.eea.kafka.configuration.KafkaConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.redis.RedisReactiveHealthIndicatorAutoConfiguration;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * The type Eea health indicator configuration.
 */
@Configuration
@AutoConfigureAfter(value = {DataSourceAutoConfiguration.class, KafkaConfiguration.class,
    RedisAutoConfiguration.class,
    RedisReactiveHealthIndicatorAutoConfiguration.class})

public class EEAHealthIndicatorConfiguration {

  /**
   * Builds a Db health indicator health indicator.
   *
   * @param dataSources the data sources
   *
   * @return the health indicator
   */
  @Bean
  @ConditionalOnBean(DataSource.class)
  @Autowired
  public HealthIndicator dbHealthIndicator(Map<String, DataSource> dataSources) {
    return new EEADataSourceHealthIndicator(dataSources);
  }

  /**
   * Builds a Kafka health indicator.
   *
   * @param adminClient the admin client
   *
   * @return the health indicator
   */
  @Bean
  @ConditionalOnBean(AdminClient.class)
  @Autowired
  public HealthIndicator kafkaHealthIndicator(AdminClient adminClient) {
    final DescribeClusterOptions describeClusterOptions =
        new DescribeClusterOptions().timeoutMs(1000);

    return () -> {
      final DescribeClusterResult describeCluster =
          adminClient.describeCluster(describeClusterOptions);
      try {
        final String clusterId = describeCluster.clusterId().get();
        final int nodeCount = describeCluster.nodes().get().size();
        return Health.up().withDetail("clusterId", clusterId).withDetail("nodeCount", nodeCount)
            .build();
      } catch (final InterruptedException | ExecutionException e) {
        // //NOSONAR false positive, I really need the thread to go on since this is a healtchecker.
        // Exception is managed by Spring Actuator
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
        return Health.down().withException(e).build();
      }
    };

  }

  /**
   * Builds a Redis health indicator health indicator.
   *
   * @param redisConnectionFactory the redis connection factory
   *
   * @return the health indicator
   */
  @Bean
  @ConditionalOnBean(RedisConnectionFactory.class)
  @Autowired
  public HealthIndicator redisHealthIndicator(RedisConnectionFactory redisConnectionFactory) {
    return new EEARedisHealthIndicator(redisConnectionFactory);
  }
}
