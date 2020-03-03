package org.eea.interfaces.vo.dataset.schemas.rule.enums;

public enum RuleOperatorEnum {
  EQ("=="), DIST("!="), GT(">"), LT("<"), GTEQ(">="), LTEQ("<="), AND("&&"), OR("||");

  private final String value;

  RuleOperatorEnum(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
