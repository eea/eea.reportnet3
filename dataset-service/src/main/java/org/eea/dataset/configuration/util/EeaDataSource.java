package org.eea.dataset.configuration.util;

import java.sql.Connection;
import java.sql.SQLException;
import org.eea.multitenancy.TenantResolver;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * The type Eea data source.
 */
public class EeaDataSource extends DriverManagerDataSource {

  @Override
  public String getSchema() {
    return TenantResolver.getTenantName();
  }


  @Override
  public Connection getConnection() throws SQLException {
    String schemaName = TenantResolver.getTenantName();
    Connection connection = super.getConnection();
    connection.setSchema(schemaName);
    return connection;
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    String schemaName = TenantResolver.getTenantName();
    Connection connection = super.getConnection(username, password);
    connection.setSchema(schemaName);
    return connection;
  }

}
