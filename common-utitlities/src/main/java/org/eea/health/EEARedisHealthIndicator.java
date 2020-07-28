package org.eea.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.redis.RedisHealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import lombok.extern.slf4j.Slf4j;

/** The Constant log. */
@Slf4j
public class EEARedisHealthIndicator extends RedisHealthIndicator {

  /** The check frenquency. */
  @Value("${spring.health.redis.check.frequency}")
  private Long checkFrenquency;

  /** The last execution time stamp. */
  private Long lastExecutionTimeStamp = 0l;

  /** The last result. */
  private Health lastResult = null;

  /**
   * Instantiates a new EEA redis health indicator.
   *
   * @param connectionFactory the connection factory
   */
  public EEARedisHealthIndicator(RedisConnectionFactory connectionFactory) {
    super(connectionFactory);
  }

  /**
   * Do health check.
   *
   * @param builder the builder
   * @throws Exception the exception
   */
  @Override
  protected void doHealthCheck(Health.Builder builder) throws Exception {
    Long currentTime = System.currentTimeMillis();
    synchronized (this) {
      Long dif = System.currentTimeMillis() - lastExecutionTimeStamp;
      if (dif > checkFrenquency) {
        log.debug("Performing redis health check");
        lastExecutionTimeStamp = currentTime;
        super.doHealthCheck(builder);
        lastResult = builder.build();
      } else {
        builder.up().withDetails(lastResult.getDetails());
      }
    }
  }
}
