package org.eea.interfaces.vo.recordstore;

import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Connection data vo.
 */
@Getter
@Setter
@ToString
public class ConnectionDataVO implements Serializable {

  private static final long serialVersionUID = 3018219891766151762L;
  private String connectionString;
  private String user;
  private String password;
  private String schema;

  @Override
  public int hashCode() {
    return Objects.hash(connectionString, user, password, schema);
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
    return connectionString.equals(that.connectionString) && user.equals(that.user)
        && password.equals(that.password) && schema.equals(that.schema);
  }
}
