package org.eea.interfaces.vo.dataset.schemas.rule;

import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class RulesSchemaVO.
 */
@Getter
@Setter
@ToString
public class RulesSchemaVO {

  /** The rules schema id. */
  private String rulesSchemaId;

  /** The id dataset schema. */
  private String idDatasetSchema;

  /** The rules. */
  private List<RuleVO> rules;


  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(rulesSchemaId, idDatasetSchema, rules);
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
    RulesSchemaVO other = (RulesSchemaVO) obj;
    return Objects.equals(rulesSchemaId, other.rulesSchemaId)
        && Objects.equals(idDatasetSchema, other.idDatasetSchema)
        && Objects.equals(rules, other.rules);
  }


}
