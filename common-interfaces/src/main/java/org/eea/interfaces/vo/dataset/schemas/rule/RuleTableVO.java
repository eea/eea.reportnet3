package org.eea.interfaces.vo.dataset.schemas.rule;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class RuleTableVO.
 */
@Getter
@Setter
@ToString
public class RuleTableVO extends RuleVO {


  /** The id table schema. */
  private String idTableSchema;

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
    result = PRIME * result + Objects.hash(idTableSchema);
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
    RuleTableVO other = (RuleTableVO) obj;
    return Objects.equals(idTableSchema, other.idTableSchema);
  }


}
