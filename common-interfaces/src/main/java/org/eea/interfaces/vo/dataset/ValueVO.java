package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.enums.DataType;
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
  private DataType type;

  /**
   * The value.
   */
  private String value;


  /** The label. */
  private String label;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(type, value);
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
    return Objects.equals(type, other.type) && Objects.equals(value, other.value);
  }

}
