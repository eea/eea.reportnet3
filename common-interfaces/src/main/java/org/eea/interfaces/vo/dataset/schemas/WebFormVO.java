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
public class WebFormVO implements Serializable {

  /**
   * The webFormName.
   */
  private String webFormName;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {

    return Objects.hash(webFormName);

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
    WebFormVO other = (WebFormVO) obj;
    return Objects.equals(webFormName, other.webFormName);
  }
}
