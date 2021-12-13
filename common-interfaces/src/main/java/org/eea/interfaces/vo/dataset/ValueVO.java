package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



/**
 * The Class ValueVO.
 */
@Getter
@Setter
@ToString
public class ValueVO implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -7417626227682871271L;

  /**
   * The type.
   */
  private String table;

  /**
   * The value.
   */
  private String value;


  /** The label. */
  private String label;

  /** The label. */
  private int row;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(table, value);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   *
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ValueVO other = (ValueVO) obj;
    return Objects.equals(table, other.table) && Objects.equals(value, other.value);
  }

}
