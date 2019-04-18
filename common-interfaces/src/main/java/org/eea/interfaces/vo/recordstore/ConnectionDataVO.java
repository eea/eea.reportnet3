package org.eea.interfaces.vo.recordstore;

import java.io.Serializable;
import java.util.Objects;

/**
 * The type Connection data vo.
 */
public class ConnectionDataVO implements Serializable {

  private static final long serialVersionUID = 3018219891766151762L;
  private String connectionString;
  private String user;
  private String password;
  private String schema;

  /**
   * Method getConnectionString returns the connectionString of this ConnectionDataVO object.
   *
   * @return the connectionString (type String) of this ConnectionDataVO object.
   */
  public String getConnectionString() {
    return connectionString;
  }

  /**
   * Method setConnectionString sets the connectionString of this ConnectionDataVO object.
   *
   * @param connectionString the connectionString of this ConnectionDataVO object.
   */
  public void setConnectionString(final String connectionString) {
    this.connectionString = connectionString;
  }

  /**
   * Method getUser returns the user of this ConnectionDataVO object.
   *
   * @return the user (type String) of this ConnectionDataVO object.
   */
  public String getUser() {
    return user;
  }

  /**
   * Method setUser sets the user of this ConnectionDataVO object.
   *
   * @param user the user of this ConnectionDataVO object.
   */
  public void setUser(final String user) {
    this.user = user;
  }

  /**
   * Method getPassword returns the password of this ConnectionDataVO object.
   *
   * @return the password (type String) of this ConnectionDataVO object.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Method setPassword sets the password of this ConnectionDataVO object.
   *
   * @param password the password of this ConnectionDataVO object.
   */
  public void setPassword(final String password) {
    this.password = password;
  }

  /**
   * Gets schema.
   *
   * @return the schema
   */
  public String getSchema() {
    return schema;
  }

  /**
   * Sets schema.
   *
   * @param schema the schema
   */
  public void setSchema(final String schema) {
    this.schema = schema;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ConnectionDataVO that = (ConnectionDataVO) o;
    return connectionString.equals(that.connectionString) &&
        user.equals(that.user) &&
        password.equals(that.password) &&
        schema.equals(that.schema);
  }

  @Override
  public int hashCode() {
    return Objects.hash(connectionString, user, password, schema);
  }

  @Override
  public String toString() {
    return "ConnectionDataVO{" +
        "connectionString='" + connectionString + '\'' +
        ", user='" + user + '\'' +
        ", password='" + password + '\'' +
        ", schema='" + schema + '\'' +
        '}';
  }
}
