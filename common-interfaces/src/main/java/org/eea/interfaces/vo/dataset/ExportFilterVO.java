package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;



/**
 * The Class ExportFilterVO.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ExportFilterVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -1206909692097622497L;

  /** The level error. */
  private ErrorTypeEnum[] levelError;

  /** The id rules. */
  private String idRules;

  /** The field value. */
  private String fieldValue;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(levelError, idRules, fieldValue);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   *
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final ExportFilterVO other = (ExportFilterVO) obj;
    return Objects.equals(levelError, other.levelError) && Objects.equals(idRules, other.idRules)
        && Objects.equals(fieldValue, other.fieldValue);
  }


}
