package org.eea.interfaces.vo.dataset.schemas;

import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter
@Getter
@ToString
public class WebformConfigVO implements Serializable {


  /**
   * 
   */
  private static final long serialVersionUID = 3025055091940745337L;

  /**
   * The id.
   */
  private Long idReferenced;

  /**
   * The webFormName.
   */
  private String name;


  private String content;

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
    WebformConfigVO other = (WebformConfigVO) obj;
    return Objects.equals(name, other.name);
  }
}
