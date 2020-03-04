package org.eea.interfaces.vo.dataset.schemas.rule.enums;

import java.util.HashMap;
import java.util.Map;

public enum RuleOperatorEnum {

  // Arithmetic operators
  EQ("==", false), DIST("!=", false), GT(">", false), LT("<", false), GTEQ(">=", false), LTEQ("<=",
      false),

  // Logical operators
  AND("&&", false), OR("||", false), NOT("!", true),

  // String operators
  SEQ("equals", true), SEQIC("equalsIgnoreCase", true);

  private final String label;

  private final boolean isBasic;

  private final static Map<String, RuleOperatorEnum> map;

  static {
    map = new HashMap<>();
    for (RuleOperatorEnum e : values()) {
      map.put(e.getValue(), e);
    }
  }

  RuleOperatorEnum(String label, boolean isBasic) {
    this.label = label;
    this.isBasic = isBasic;
  }

  public static RuleOperatorEnum valueOfLabel(String label) {
    return map.get(label);
  }

  public String getValue() {
    return label;
  }

  public boolean isBasic() {
    return isBasic;
  }
}
