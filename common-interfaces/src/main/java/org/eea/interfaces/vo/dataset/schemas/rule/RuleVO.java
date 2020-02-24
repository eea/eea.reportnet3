package org.eea.interfaces.vo.dataset.schemas.rule;

import java.util.List;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Rule.
 */
@Getter
@Setter

/**
 * To string.
 *
 * @return the java.lang. string
 */
@ToString
public class RuleVO {

  /** The rule id. */
  private String ruleId;

  /** The reference id. */
  private String referenceId;

  /** The rule name. */
  private String ruleName;

  /** The automatic. */
  private Boolean automatic;

  /** The enabled. */
  private Boolean enabled;

  /** The salience. */
  private Integer salience;

  /** The activation group. */
  private String activationGroup;

  /** The type. */
  private EntityTypeEnum type;

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
    return Objects.hash(ruleId, referenceId, ruleName, automatic, enabled, salience,
        activationGroup, type, whenCondition, thenCondition);
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
    return Objects.equals(ruleId, other.ruleId) && Objects.equals(referenceId, other.referenceId)
        && Objects.equals(ruleName, other.ruleName) && Objects.equals(automatic, other.automatic)
        && Objects.equals(enabled, other.enabled) && Objects.equals(salience, other.salience)
        && Objects.equals(activationGroup, other.activationGroup)
        && Objects.equals(type, other.type) && Objects.equals(thenCondition, other.thenCondition)
        && Objects.equals(whenCondition, other.whenCondition);
  }


}
