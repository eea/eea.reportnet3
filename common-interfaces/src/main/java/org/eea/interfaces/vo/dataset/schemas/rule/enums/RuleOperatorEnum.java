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
  F_AND(EntityTypeEnum.FIELD, "fAnd", "Boolean", "Boolean", "Boolean"),
  /** Or. */
  F_OR(EntityTypeEnum.FIELD, "fOr", "Boolean", "Boolean", "Boolean"),
  /** Not. */
  F_NOT(EntityTypeEnum.FIELD, "not", "Boolean", "Boolean"),

  // Number operators
  /** Equals. */
  F_EQ(EntityTypeEnum.FIELD, "numberEquals", "Boolean", "Number", "Number"),
  /** Distinct. */
  F_DIST(EntityTypeEnum.FIELD, "distinct", "Boolean", "Number", "Number"),
  /** Greater than. */
  F_GT(EntityTypeEnum.FIELD, ">", "greaterThanNumber", "Number", "Number"),
  /** Less than. */
  F_LT(EntityTypeEnum.FIELD, "<", "lessThanNumber", "Number", "Number"),
  /** Greater than or equals. */
  F_GTEQ(EntityTypeEnum.FIELD, ">=", "greaterThanOrEqualsThanNumber", "Number", "Number"),
  /** Less than or equals. */
  F_LTEQ(EntityTypeEnum.FIELD, "<=", "lessThanOrEqualsThanNumber", "Number", "Number"),
  /** The num match. */
  F_NUM_MATCH(EntityTypeEnum.FIELD, "numberMatch", "Boolean", "Number", "String"),

  // String operators
  /** Length. */
  F_LEN(EntityTypeEnum.FIELD, "length", "Number", "String"),
  /** Equals for strings. */
  F_SEQ(EntityTypeEnum.FIELD, "equals", "Boolean", "String", "String"),
  /** Equals for string ignoring case. */
  F_SEQIC(EntityTypeEnum.FIELD, "equalsIgnoreCase", "Boolean", "String", "String"),
  /** Match. */
  F_MATCH(EntityTypeEnum.FIELD, "matches", "Boolean", "String", "String"),

  // Day operators
  /** The eq day. */
  F_EQ_DAY(EntityTypeEnum.FIELD, "equalDay", "Boolean", "Date", "Number"),
  /** The dist day. */
  F_DIST_DAY(EntityTypeEnum.FIELD, "distinctDay", "Boolean", "Date", "Number"),
  /** The gt day. */
  F_GT_DAY(EntityTypeEnum.FIELD, "greaterThanDay", "Boolean", "Date", "Number"),
  /** The lt day. */
  F_LT_DAY(EntityTypeEnum.FIELD, "lessThanDay", "Boolean", "Date", "Number"),
  /** The gteq day. */
  F_GTEQ_DAY(EntityTypeEnum.FIELD, "greaterThanOrEqualsThanDay", "Boolean", "Date", "Number"),
  /** The lteq day. */
  F_LTEQ_DAY(EntityTypeEnum.FIELD, "lessThanOrEqualsThanDay", "Boolean", "Date", "Number"),

  // Month operators
  /** The eq month. */
  F_EQ_MONTH(EntityTypeEnum.FIELD, "equalMonth", "Boolean", "Date", "Number"),
  /** The dist month. */
  F_DIST_MONTH(EntityTypeEnum.FIELD, "distinctMonth", "Boolean", "Date", "Number"),
  /** The gt month. */
  F_GT_MONTH(EntityTypeEnum.FIELD, "greaterThanMonth", "Boolean", "Date", "Number"),
  /** The lt month. */
  F_LT_MONTH(EntityTypeEnum.FIELD, "lessThanMonth", "Boolean", "Date", "Number"),
  /** The gteq month. */
  F_GTEQ_MONTH(EntityTypeEnum.FIELD, "greaterThanOrEqualsThanMonth", "Boolean", "Date", "Number"),
  /** The lteq month. */
  F_LTEQ_MONTH(EntityTypeEnum.FIELD, "lessThanOrEqualsThanMonth", "Boolean", "Date", "Number"),

  // Year operators
  /** The eq year. */
  F_EQ_YEAR(EntityTypeEnum.FIELD, "equalYear", "Boolean", "Date", "Number"),
  /** The dist year. */
  F_DIST_YEAR(EntityTypeEnum.FIELD, "distinctYear", "Boolean", "Date", "Number"),
  /** The gt year. */
  F_GT_YEAR(EntityTypeEnum.FIELD, "greaterThanYear", "Boolean", "Date", "Number"),
  /** The lt year. */
  F_LT_YEAR(EntityTypeEnum.FIELD, "lessThanYear", "Boolean", "Date", "Number"),
  /** The gteq year. */
  F_GTEQ_YEAR(EntityTypeEnum.FIELD, "greaterThanOrEqualsThanYear", "Boolean", "Date", "Number"),
  /** The lteq year. */
  F_LTEQ_YEAR(EntityTypeEnum.FIELD, "lessThanOrEqualsThanYear", "Boolean", "Date", "Number"),

  // Date operators
  /** The eq date. */
  F_EQ_DATE(EntityTypeEnum.FIELD, "equalDate", "Boolean", "Date", "Date"),
  /** The dist date. */
  F_DIST_DATE(EntityTypeEnum.FIELD, "distinctDate", "Boolean", "Date", "Date"),
  /** The gt date. */
  F_GT_DATE(EntityTypeEnum.FIELD, "greaterThanDate", "Boolean", "Date", "Date"),
  /** The lt date. */
  F_LT_DATE(EntityTypeEnum.FIELD, "lessThanDate", "Boolean", "Date", "Date"),
  /** The gteq date. */
  F_GTEQ_DATE(EntityTypeEnum.FIELD, "greaterThanOrEqualsThanDate", "Boolean", "Date", "Date"),
  /** The lteq date. */
  F_LTEQ_DATE(EntityTypeEnum.FIELD, "lessThanOrEqualsThanDate", "Boolean", "Date", "Date");

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
