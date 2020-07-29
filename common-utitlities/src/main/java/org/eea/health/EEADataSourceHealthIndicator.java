package org.eea.health;

import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import lombok.extern.slf4j.Slf4j;


/** The Constant log. */
@Slf4j
public class EEADataSourceHealthIndicator extends DataSourceHealthIndicator {

  /** The last execution time stamp. */
  private Long lastExecutionTimeStamp = 0l;

  /** The last result. */
  private Health lastResult = null;

  /** The check frenquency. */
  @Value("${spring.health.db.check.frequency}")
  private Long checkFrenquency;

  /**
   * Instantiates a new EEA data source health indicator.
   *
   * @param dataSources the data sources
   */
  @Autowired
  public EEADataSourceHealthIndicator(Map<String, DataSource> dataSources) {
    super(dataSources.values().stream().findFirst().orElseThrow(IllegalStateException::new),
        "SELECT 1");
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
        log.debug("Performing data base health check");
        lastExecutionTimeStamp = currentTime;
        super.doHealthCheck(builder);
        lastResult = builder.build();
      } else {
        builder.up().withDetails(lastResult.getDetails());
      }
    }

  }

}
