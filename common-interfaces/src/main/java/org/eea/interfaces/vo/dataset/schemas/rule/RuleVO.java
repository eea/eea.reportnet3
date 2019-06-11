package org.eea.interfaces.vo.dataset.schemas.rule;

import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Rule.
 */
@Getter
@Setter
@ToString
public class RuleVO {

  /** The rule id. */
  private String ruleId;

  /** The data flow id. */
  private Long dataFlowId;

  /** The rule name. */
  private String ruleName;

  /** The when condition. */
  private String whenCondition;

  /** The then condition. */
  private List<String> thenCondition;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(dataFlowId, ruleId, ruleName, thenCondition, whenCondition);
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
    RuleVO other = (RuleVO) obj;
    return Objects.equals(dataFlowId, other.dataFlowId) && Objects.equals(ruleId, other.ruleId)
        && Objects.equals(ruleName, other.ruleName)
        && Objects.equals(thenCondition, other.thenCondition)
        && Objects.equals(whenCondition, other.whenCondition);
  }


}
