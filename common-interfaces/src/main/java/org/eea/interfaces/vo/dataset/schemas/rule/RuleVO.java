package org.eea.interfaces.vo.dataset.schemas.rule;

import java.util.List;
import java.util.Objects;
import org.eea.interfaces.dto.dataset.schemas.rule.RuleExpressionDTO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.enums.AutomaticRuleTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Rule VO.
 */
@Getter
@Setter
@ToString
public class RuleVO {

  /** The rule id. */
  private String ruleId;

  /** The reference id. */
  private String referenceId;

  /** The rule name. */
  private String ruleName;

  /** The automatic. */
  private boolean automatic;

  /** The enabled. */
  private boolean enabled;

  /** The verified. */
  private Boolean verified;

  /** The salience. */
  private Integer salience;

  /** The activation group. */
  private String activationGroup;

  /** The type. */
  private EntityTypeEnum type;

  /** The when condition. */
  private RuleExpressionDTO whenCondition;

  /** The then condition. */
  private List<String> thenCondition;

  /** The description. */
  private String description;

  /** The short code. */
  private String shortCode;

  /** The automatic type. */
  private AutomaticRuleTypeEnum automaticType;

  /** The integrity VO. */
  private IntegrityVO integrityVO;

  /** The SQLSentence. */
  private String sqlSentence;

  /** The expression text. */
  private String expressionText;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(ruleId, referenceId, ruleName, automatic, enabled, salience,
        activationGroup, type, whenCondition, thenCondition, description, shortCode, integrityVO,
        sqlSentence);
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
        && Objects.equals(whenCondition, other.whenCondition)
        && Objects.equals(description, other.description)
        && Objects.equals(shortCode, other.shortCode)
        && Objects.equals(integrityVO, other.integrityVO)
        && Objects.equals(sqlSentence, other.sqlSentence);
  }
}
