package org.eea.dataset.multitenancy;

import java.util.Map;
import javax.sql.DataSource;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * The type Multi tenant data source.
 */
public class MultiTenantDataSource extends AbstractRoutingDataSource {

  /** The data sources. */
  @Autowired
  @Qualifier("targetDataSources")
  private Map<Object, Object> dataSources;

  /**
   * Determine current lookup key.
   *
   * @return the object
   */
  @Override
  protected Object determineCurrentLookupKey() {
    return TenantResolver.getTenantName();
  }

  /**
   * Add data source.
   *
   * @param connectionDataVO the connection data vo
   */
  public void addDataSource(ConnectionDataVO connectionDataVO) {
    dataSources.put(connectionDataVO.getSchema(), createDataSource(connectionDataVO));
    synchronized (this) {
      setTargetDataSources(dataSources);
      afterPropertiesSet();
    }
  }

  /**
   * Creates the data source.
   *
   * @param connectionDataVO the connection data VO
   * @return the data source
   */
  private DataSource createDataSource(ConnectionDataVO connectionDataVO) {
    DriverManagerDataSource ds = new DriverManagerDataSource();
    ds.setUrl(connectionDataVO.getConnectionString());
    ds.setUsername(connectionDataVO.getUser());
    ds.setPassword(connectionDataVO.getPassword());
    ds.setDriverClassName("org.postgresql.Driver");
    ds.setSchema(connectionDataVO.getSchema());

    return ds;
  }
}
