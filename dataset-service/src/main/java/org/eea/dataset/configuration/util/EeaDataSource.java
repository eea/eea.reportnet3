package org.eea.dataset.configuration.util;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.eea.multitenancy.TenantResolver;

/**
 * The type Eea data source.
 */
public class EeaDataSource extends HikariDataSource {

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
