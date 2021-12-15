package org.eea.interfaces.vo.dataset.schemas.rule.enums;

/**
 * The Enum AutomaticRuleTypeEnum.
 */
public enum AutomaticRuleTypeEnum {

  /** The field type. Default short code:FT */
  FIELD_TYPE("FIELD_TYPE"),

  /** The field sql type. Default short code:FT */
  FIELD_SQL_TYPE("FIELD_SQL_TYPE"),

  /** The field cardinality. Default short code:FC */
  FIELD_CARDINALITY("FIELD_CARDINALITY"),

  /** The table completness. Default short code:TO */
  TABLE_COMPLETNESS("TABLE_COMPLETNESS"),

  /** The field link. Default short code:TC */
  FIELD_LINK("FIELD_LINK"),

  /** The table uniqueness. Default short code:TU */
  TABLE_UNIQUENESS("TABLE_UNIQUENESS"),

  /** The mandatory table. Default short code:TB */
  MANDATORY_TABLE("MANDATORY_TABLE");


  /** The value. */
  private final String value;

  /**
   * Instantiates a new type entity enum.
   *
   * @param value the value
   */
  AutomaticRuleTypeEnum(String value) {
    this.value = value;
  }

  /**
   * Gets the value.
   *
   * @return the value
   */
  public String getValue() {
    return value;
  }

}
