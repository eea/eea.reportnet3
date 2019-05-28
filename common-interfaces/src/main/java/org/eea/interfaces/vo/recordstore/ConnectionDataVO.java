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

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 3018219891766151762L;

  /** The connection string. */
  private String connectionString;

  /** The user. */
  private String user;

  /** The password. */
  private String password;

  /** The schema. */
  private String schema;
  
  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(connectionString, user, password, schema);
  }

   /**
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
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
