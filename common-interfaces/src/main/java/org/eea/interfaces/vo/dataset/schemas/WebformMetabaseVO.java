package org.eea.interfaces.vo.dataset.schemas;

import java.io.Serializable;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.enums.WebformTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class WebformMetabaseVO.
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


  /** The label. */
  private String label;

  /** The value. */
  private String value;

  /** The type. */
  private WebformTypeEnum type;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {

    return Objects.hash(id, label, value, type);

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
    return Objects.equals(label, other.label) && Objects.equals(value, other.value)
        && Objects.equals(type, other.type);
  }
}
