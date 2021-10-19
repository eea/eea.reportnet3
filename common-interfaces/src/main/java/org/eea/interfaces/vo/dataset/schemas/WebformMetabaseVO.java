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
public class WebformMetabaseVO implements Serializable {


  /**
   *
   */
  private static final long serialVersionUID = 5928873289242363790L;

  /** The id. */
  private Long id;

  /**
   * The webFormName.
   */
  private String name;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {

    return Objects.hash(name);

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
    WebformMetabaseVO other = (WebformMetabaseVO) obj;
    return Objects.equals(name, other.name);
  }
}
