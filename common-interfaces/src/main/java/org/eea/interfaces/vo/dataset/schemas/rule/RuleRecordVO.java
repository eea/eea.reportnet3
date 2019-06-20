package org.eea.interfaces.vo.dataset.schemas.rule;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class RuleRecordVO.
 */
@Getter
@Setter
@ToString
public class RuleRecordVO extends RuleVO {

  /** The id record schema. */
  private String idRecordSchema;

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
    result = PRIME * result + Objects.hash(idRecordSchema);
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
    RuleRecordVO other = (RuleRecordVO) obj;
    return Objects.equals(idRecordSchema, other.idRecordSchema);
  }

}
