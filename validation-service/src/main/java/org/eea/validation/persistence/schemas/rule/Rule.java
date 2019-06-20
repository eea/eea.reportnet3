package org.eea.validation.persistence.schemas.rule;

import java.util.List;
import java.util.Objects;
import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Rule.
 */
@Getter
@Setter
@ToString
public class Rule {

  /** The rule id. */
  @Id
  @Field(value = "_id")
  private ObjectId ruleId;

  /** The id data flow. */
  @Field(value = "id_DataFlow")
  private Long dataFlowId;

  /** The rule name. */
  @Field(value = "ruleName")
  private String ruleName;

  /** The when condition. */
  @Field(value = "whenCondition")
  private String whenCondition;

  /** The then condition. */
  @Field(value = "thenCondition")
  private List<String> thenCondition;

  @Field(value = "scope")
  private TypeEntityEnum scope;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(dataFlowId, ruleId, ruleName, thenCondition, whenCondition, scope);
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
    Rule other = (Rule) obj;
    return Objects.equals(dataFlowId, other.dataFlowId) && Objects.equals(ruleId, other.ruleId)
        && Objects.equals(ruleName, other.ruleName)
        && Objects.equals(thenCondition, other.thenCondition)
        && Objects.equals(whenCondition, other.whenCondition) && Objects.equals(scope, other.scope);
  }
}
