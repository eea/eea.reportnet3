package org.eea.health;

import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;


@Slf4j
public class EEADataSourceHealthIndicator extends DataSourceHealthIndicator {

  private Long lastExecutionTimeStamp = 0l;
  private Health.Builder lasResult = null;

  @Value("${spring.health.db.check.frequency}")
  private Long checkFrenquency;

  @Autowired
  public EEADataSourceHealthIndicator(Map<String, DataSource> dataSources) {
    super(dataSources.values().stream().findFirst().get(), "SELECT 1");
  }

  @Override
  protected void doHealthCheck(Health.Builder builder) throws Exception {
    Long currentTime = System.currentTimeMillis();
    synchronized (this) {
      Long dif = System.currentTimeMillis() - lastExecutionTimeStamp;
      if (dif > checkFrenquency) {
        log.debug("Performing data base health check");
        lastExecutionTimeStamp = currentTime;
        super.doHealthCheck(builder);
        lasResult = builder;
      } else {
        builder = lasResult;
      }
    }

  }
}
