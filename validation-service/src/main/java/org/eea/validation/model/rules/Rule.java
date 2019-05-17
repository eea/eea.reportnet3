package org.eea.validation.model.rules;

import java.util.Objects;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Rule {

  /** The id rule. */
  @Id
  private Integer idRule;

  /** The rules base. */
  private String rulesBase;

  /** The rules session. */
  private String rulesSession;

  /** The rules agent. */
  private String rulesAgent;

  /** The name. */
  private String name;

  /** The attribute. */
  private String attribute;

  /** The conditional element. */
  private String conditionalElement;

  /** The action. */
  private String action;

  @Override
  public int hashCode() {
    return Objects.hash(action, attribute, conditionalElement, idRule, name, rulesAgent, rulesBase,
        rulesSession);
  }

  /**
   * 
   *
   * @param obj
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Rule other = (Rule) obj;
    return Objects.equals(action, other.action) && Objects.equals(attribute, other.attribute)
        && Objects.equals(conditionalElement, other.conditionalElement)
        && Objects.equals(idRule, other.idRule) && Objects.equals(name, other.name)
        && Objects.equals(rulesAgent, other.rulesAgent)
        && Objects.equals(rulesBase, other.rulesBase)
        && Objects.equals(rulesSession, other.rulesSession);
  }

}
