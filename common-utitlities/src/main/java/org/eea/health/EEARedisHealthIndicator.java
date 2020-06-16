package org.eea.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.redis.RedisHealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Slf4j
public class EEARedisHealthIndicator extends RedisHealthIndicator {

  @Value("${spring.health.redis.check.frequency}")
  private Long checkFrenquency;

  private Long lastExecutionTimeStamp = 0l;
  private Health lastResult = null;

  public EEARedisHealthIndicator(
      RedisConnectionFactory connectionFactory) {
    super(connectionFactory);
  }

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
