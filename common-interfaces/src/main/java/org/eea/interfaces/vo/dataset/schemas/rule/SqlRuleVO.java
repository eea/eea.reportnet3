package org.eea.interfaces.vo.dataset.schemas.rule;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class SqlRuleVO.
 */
@Getter
@Setter
@ToString
public class SqlRuleVO {

  /** The sql rule. */
  private String sqlRule;



  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(sqlRule);
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
    SqlRuleVO other = (SqlRuleVO) obj;
    return Objects.equals(sqlRule, other.sqlRule);
  }
}
