package org.eea.interfaces.vo.dataset.schemas;

import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class WebFormVO.
 */
@Setter
@Getter
@ToString
public class WebformVO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 377071281084102044L;

  /**
   * The webFormName.
   */
  private String name;

  /** The type. */
  private String type;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {

    return Objects.hash(name, type);

  }

  /**
   * Equals.
   *
   * @param obj the obj
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
    WebformVO other = (WebformVO) obj;
    return Objects.equals(name, other.name) && Objects.equals(type, other.type);
  }
}
