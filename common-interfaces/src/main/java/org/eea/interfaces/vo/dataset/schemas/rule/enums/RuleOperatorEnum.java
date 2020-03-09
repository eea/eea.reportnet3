package org.eea.interfaces.vo.dataset.schemas.rule.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * The Enum RuleOperatorEnum.
 */
public enum RuleOperatorEnum {

  // Arithmetic operators
  EQ("=="), DIST("!="), GT(">"), LT("<"), GTEQ(">="), LTEQ("<="),

  // Logical operators
  AND("&&"), OR("||"), NOT("!"),

  // Functions
  LEN("length"), SEQ("equals"), SEQIC("equalsIgnoreCase");

  /** Operator's Java representation. */
  private final String label;

  /** Transformation between RuleOperatorEnum and Java representation. */
  private final static Map<String, RuleOperatorEnum> map;

  static {
    map = new HashMap<>();
    for (RuleOperatorEnum e : values()) {
      map.put(e.getLabel(), e);
    }
  }

  /**
   * Instantiates a new RuleOperatorEnum. Should not be used.
   *
   * @param label the label
   */
  private RuleOperatorEnum(String label) {
    this.label = label;
  }

  /**
   * Gets a RuleOperatorEnum by its label (Java representation).
   *
   * @param label the label
   * @return the rule operator enum
   */
  public static RuleOperatorEnum valueOfLabel(String label) {
    return map.get(label);
  }

  /**
   * Gets the label.
   *
   * @return the label
   */
  public String getLabel() {
    return label;
  }
}
