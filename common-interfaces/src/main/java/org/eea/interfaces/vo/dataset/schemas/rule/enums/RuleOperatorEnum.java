package org.eea.interfaces.vo.dataset.schemas.rule.enums;

import java.util.HashMap;
import java.util.Map;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;

/**
 * The Enum RuleOperatorEnum.
 */
public enum RuleOperatorEnum {

  // Logical operators
  /** And. */
  F_AND(EntityTypeEnum.FIELD, "fieldAnd", "Boolean", "Boolean", "Boolean"),
  /** Or. */
  F_OR(EntityTypeEnum.FIELD, "fieldOr", "Boolean", "Boolean", "Boolean"),
  /** Not. */
  F_NOT(EntityTypeEnum.FIELD, "fieldNot", "Boolean", "Boolean"),

  // Number operators
  /** Equals. */
  F_EQ(EntityTypeEnum.FIELD, "numberEquals", "Boolean", "Number", "Number"),
  /** Distinct. */
  F_DIST(EntityTypeEnum.FIELD, "numberDistinct", "Boolean", "Number", "Number"),
  /** Greater than. */
  F_GT(EntityTypeEnum.FIELD, "numberGreaterThan", "Boolean", "Number", "Number"),
  /** Less than. */
  F_LT(EntityTypeEnum.FIELD, "numberLessThan", "Boolean", "Number", "Number"),
  /** Greater than or equals. */
  F_GTEQ(EntityTypeEnum.FIELD, "numberGreaterThanOrEqualsThan", "Boolean", "Number", "Number"),
  /** Less than or equals. */
  F_LTEQ(EntityTypeEnum.FIELD, "numberLessThanOrEqualsThan", "Boolean", "Number", "Number"),
  /** The num match. */
  F_NUM_MATCH(EntityTypeEnum.FIELD, "numberMatches", "Boolean", "Number", "String"),

  // String operators
  /** Length. */
  F_LEN(EntityTypeEnum.FIELD, "stringLength", "Number", "String"),
  /** Equals for strings. */
  F_SEQ(EntityTypeEnum.FIELD, "stringEquals", "Boolean", "String", "String"),
  /** Equals for string ignoring case. */
  F_SEQIC(EntityTypeEnum.FIELD, "stringEqualsIgnoreCase", "Boolean", "String", "String"),
  /** Match. */
  F_MATCH(EntityTypeEnum.FIELD, "stringMatches", "Boolean", "String", "String"),

  // Day operators
  /** The eq day. */
  F_EQ_DAY(EntityTypeEnum.FIELD, "dayEquals", "Boolean", "Date", "Number"),
  /** The dist day. */
  F_DIST_DAY(EntityTypeEnum.FIELD, "dayDistinct", "Boolean", "Date", "Number"),
  /** The gt day. */
  F_GT_DAY(EntityTypeEnum.FIELD, "dayGreaterThan", "Boolean", "Date", "Number"),
  /** The lt day. */
  F_LT_DAY(EntityTypeEnum.FIELD, "dayLessThan", "Boolean", "Date", "Number"),
  /** The gteq day. */
  F_GTEQ_DAY(EntityTypeEnum.FIELD, "dayGreaterThanOrEqualsThan", "Boolean", "Date", "Number"),
  /** The lteq day. */
  F_LTEQ_DAY(EntityTypeEnum.FIELD, "dayLessThanOrEqualsThan", "Boolean", "Date", "Number"),

  // Month operators
  /** The eq month. */
  F_EQ_MONTH(EntityTypeEnum.FIELD, "monthEquals", "Boolean", "Date", "Number"),
  /** The dist month. */
  F_DIST_MONTH(EntityTypeEnum.FIELD, "monthDistinct", "Boolean", "Date", "Number"),
  /** The gt month. */
  F_GT_MONTH(EntityTypeEnum.FIELD, "monthGreaterThan", "Boolean", "Date", "Number"),
  /** The lt month. */
  F_LT_MONTH(EntityTypeEnum.FIELD, "monthLessThan", "Boolean", "Date", "Number"),
  /** The gteq month. */
  F_GTEQ_MONTH(EntityTypeEnum.FIELD, "monthGreaterThanOrEqualsThan", "Boolean", "Date", "Number"),
  /** The lteq month. */
  F_LTEQ_MONTH(EntityTypeEnum.FIELD, "monthLessThanOrEqualsThan", "Boolean", "Date", "Number"),

  // Year operators
  /** The eq year. */
  F_EQ_YEAR(EntityTypeEnum.FIELD, "yearEquals", "Boolean", "Date", "Number"),
  /** The dist year. */
  F_DIST_YEAR(EntityTypeEnum.FIELD, "yearDistinct", "Boolean", "Date", "Number"),
  /** The gt year. */
  F_GT_YEAR(EntityTypeEnum.FIELD, "yearGreaterThan", "Boolean", "Date", "Number"),
  /** The lt year. */
  F_LT_YEAR(EntityTypeEnum.FIELD, "yearLessThan", "Boolean", "Date", "Number"),
  /** The gteq year. */
  F_GTEQ_YEAR(EntityTypeEnum.FIELD, "yearGreaterThanOrEqualsThan", "Boolean", "Date", "Number"),
  /** The lteq year. */
  F_LTEQ_YEAR(EntityTypeEnum.FIELD, "yearLessThanOrEqualsThan", "Boolean", "Date", "Number"),

  // Date operators
  /** The eq date. */
  F_EQ_DATE(EntityTypeEnum.FIELD, "dateEquals", "Boolean", "Date", "Date"),
  /** The dist date. */
  F_DIST_DATE(EntityTypeEnum.FIELD, "dateDistinct", "Boolean", "Date", "Date"),
  /** The gt date. */
  F_GT_DATE(EntityTypeEnum.FIELD, "dateGreaterThan", "Boolean", "Date", "Date"),
  /** The lt date. */
  F_LT_DATE(EntityTypeEnum.FIELD, "dateLessThan", "Boolean", "Date", "Date"),
  /** The gteq date. */
  F_GTEQ_DATE(EntityTypeEnum.FIELD, "dateGreaterThanOrEqualsThan", "Boolean", "Date", "Date"),
  /** The lteq date. */
  F_LTEQ_DATE(EntityTypeEnum.FIELD, "dateLessThanOrEqualsThan", "Boolean", "Date", "Date");

  /** The entity type. */
  private final EntityTypeEnum entityType;

  /** Operator's Java representation. */
  private final String functionName;

  /** The return type. */
  private final String returnType;

  /** The input types. */
  private final String[] paramTypes;

  /** Transformation between RuleOperatorEnum and Java representation. */
  private static final Map<String, RuleOperatorEnum> map;

  static {
    map = new HashMap<>();
    for (RuleOperatorEnum e : values()) {
      map.put(e.getFunctionName(), e);
    }
  }

  /**
   * Instantiates a new RuleOperatorEnum. Should not be used.
   *
   * @param entityType the entity type
   * @param functionName the function name
   * @param returnType the return type
   * @param paramTypes the param types
   */
  private RuleOperatorEnum(EntityTypeEnum entityType, String functionName, String returnType,
      String... paramTypes) {
    this.entityType = entityType;
    this.functionName = functionName;
    this.returnType = returnType;
    this.paramTypes = paramTypes;
  }

  /**
   * Gets a RuleOperatorEnum by its label (Java representation).
   *
   * @param label the label
   * @return the rule operator enum
   */
  public static RuleOperatorEnum valueOfFunctionName(String label) {
    return map.get(label);
  }

  /**
   * Gets the entity type.
   *
   * @return the entity type
   */
  public EntityTypeEnum getEntityType() {
    return entityType;
  }

  /**
   * Gets the function name.
   *
   * @return the function name
   */
  public String getFunctionName() {
    return functionName;
  }

  /**
   * Gets the return type.
   *
   * @return the return type
   */
  public String getReturnType() {
    return returnType;
  }

  /**
   * Gets the param types.
   *
   * @return the param types
   */
  public String[] getParamTypes() {
    return paramTypes;
  }
}
