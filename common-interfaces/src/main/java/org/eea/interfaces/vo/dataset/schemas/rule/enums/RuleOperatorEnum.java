package org.eea.interfaces.vo.dataset.schemas.rule.enums;

import java.util.HashMap;
import java.util.Map;

public enum RuleOperatorEnum {

  // Arithmetic operators
  EQ("=="), DIST("!="), GT(">"), LT("<"), GTEQ(">="), LTEQ("<="),

  // Logical operators
  AND("&&"), OR("||"), NOT("!"),

  // Functions
  LEN("length"), SEQ("equals"), SEQIC("equalsIgnoreCase");

  private final String label;

  private final static Map<String, RuleOperatorEnum> map;

  static {
    map = new HashMap<>();
    for (RuleOperatorEnum e : values()) {
      map.put(e.getLabel(), e);
    }
  }

  RuleOperatorEnum(String label) {
    this.label = label;
  }

  public static RuleOperatorEnum valueOfLabel(String label) {
    return map.get(label);
  }

  public String getLabel() {
    return label;
  }
}
