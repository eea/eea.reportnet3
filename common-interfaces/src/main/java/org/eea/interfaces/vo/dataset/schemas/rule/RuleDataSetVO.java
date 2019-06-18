package org.eea.interfaces.vo.dataset.schemas.rule;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class RuleDataSetVO.
 */
@Getter
@Setter
@ToString
public class RuleDataSetVO extends RuleVO {

  /** The id data set schema. */
  private String idDataSetSchema;

  /** The Constant PRIME. */
  private static final int PRIME = 31;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = PRIME * result + Objects.hash(idDataSetSchema);
    return result;
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
    RuleDataSetVO other = (RuleDataSetVO) obj;
    return Objects.equals(idDataSetSchema, other.idDataSetSchema);
  }

}
