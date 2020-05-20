package org.eea.interfaces.vo.dataset.schemas.rule.enums;

import java.util.HashMap;
import java.util.Map;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;

/**
 * The Enum RuleOperatorEnum.
 */
public enum RuleOperatorEnum {

  // ***************************
  // ***** RECORD OPERATORS ****
  // ***************************

  /** The record if. */
  RECORD_IF(EntityTypeEnum.RECORD, "recordIfThen", JavaType.BOOLEAN, JavaType.BOOLEAN,
      JavaType.BOOLEAN),

  // ***************************
  // ***** FIELD OPERATORS *****
  // ***************************

  // Logical operators
  /** And. */
  FIELD_AND(EntityTypeEnum.FIELD, "fieldAnd", JavaType.BOOLEAN, JavaType.BOOLEAN, JavaType.BOOLEAN),
  /** Or. */
  FIELD_OR(EntityTypeEnum.FIELD, "fieldOr", JavaType.BOOLEAN, JavaType.BOOLEAN, JavaType.BOOLEAN),
  /** Not. */
  FIELD_NOT(EntityTypeEnum.FIELD, "fieldNot", JavaType.BOOLEAN, JavaType.BOOLEAN),

  // Number operators
  /** Equals. */
  FIELD_EQ(EntityTypeEnum.FIELD, "fieldNumberEquals", JavaType.BOOLEAN, JavaType.NUMBER,
      JavaType.NUMBER),
  /** Distinct. */
  FIELD_DIST(EntityTypeEnum.FIELD, "fieldNumberDistinct", JavaType.BOOLEAN, JavaType.NUMBER,
      JavaType.NUMBER),
  /** Greater than. */
  FIELD_GT(EntityTypeEnum.FIELD, "fieldNumberGreaterThan", JavaType.BOOLEAN, JavaType.NUMBER,
      JavaType.NUMBER),
  /** Less than. */
  FIELD_LT(EntityTypeEnum.FIELD, "fieldNumberLessThan", JavaType.BOOLEAN, JavaType.NUMBER,
      JavaType.NUMBER),
  /** Greater than or equals. */
  FIELD_GTEQ(EntityTypeEnum.FIELD, "fieldNumberGreaterThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.NUMBER, JavaType.NUMBER),
  /** Less than or equals. */
  FIELD_LTEQ(EntityTypeEnum.FIELD, "fieldNumberLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.NUMBER, JavaType.NUMBER),
  /** The num match. */
  FIELD_NUM_MATCH(EntityTypeEnum.FIELD, "fieldNumberMatches", JavaType.BOOLEAN, JavaType.NUMBER,
      JavaType.STRING),

  // String operators
  /** Length. */
  FIELD_LEN(EntityTypeEnum.FIELD, "fieldStringLength", JavaType.NUMBER, JavaType.STRING),
  /** Equals for strings. */
  FIELD_SEQ(EntityTypeEnum.FIELD, "fieldStringEquals", JavaType.BOOLEAN, JavaType.STRING,
      JavaType.STRING),
  /** Equals for string ignoring case. */
  FIELD_SEQIC(EntityTypeEnum.FIELD, "fieldStringEqualsIgnoreCase", JavaType.BOOLEAN,
      JavaType.STRING, JavaType.STRING),
  /** Match. */
  FIELD_MATCH(EntityTypeEnum.FIELD, "fieldStringMatches", JavaType.BOOLEAN, JavaType.STRING,
      JavaType.STRING),

  // Day operators
  /** The eq day. */
  FIELD_EQ_DAY(EntityTypeEnum.FIELD, "fieldDayEquals", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),
  /** The dist day. */
  FIELD_DIST_DAY(EntityTypeEnum.FIELD, "fieldDayDistinct", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),
  /** The gt day. */
  FIELD_GT_DAY(EntityTypeEnum.FIELD, "fieldDayGreaterThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),
  /** The lt day. */
  FIELD_LT_DAY(EntityTypeEnum.FIELD, "fieldDayLessThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),
  /** The gteq day. */
  FIELD_GTEQ_DAY(EntityTypeEnum.FIELD, "fieldDayGreaterThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),
  /** The lteq day. */
  FIELD_LTEQ_DAY(EntityTypeEnum.FIELD, "fieldDayLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),

  // Month operators
  /** The eq month. */
  FIELD_EQ_MONTH(EntityTypeEnum.FIELD, "fieldMonthEquals", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),
  /** The dist month. */
  FIELD_DIST_MONTH(EntityTypeEnum.FIELD, "fieldMonthDistinct", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),
  /** The gt month. */
  FIELD_GT_MONTH(EntityTypeEnum.FIELD, "fieldMonthGreaterThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),
  /** The lt month. */
  FIELD_LT_MONTH(EntityTypeEnum.FIELD, "fieldMonthLessThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),
  /** The gteq month. */
  FIELD_GTEQ_MONTH(EntityTypeEnum.FIELD, "fieldMonthGreaterThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),
  /** The lteq month. */
  FIELD_LTEQ_MONTH(EntityTypeEnum.FIELD, "fieldMonthLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),

  // Year operators
  /** The eq year. */
  FIELD_EQ_YEAR(EntityTypeEnum.FIELD, "fieldYearEquals", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),
  /** The dist year. */
  FIELD_DIST_YEAR(EntityTypeEnum.FIELD, "fieldYearDistinct", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),
  /** The gt year. */
  FIELD_GT_YEAR(EntityTypeEnum.FIELD, "fieldYearGreaterThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),
  /** The lt year. */
  FIELD_LT_YEAR(EntityTypeEnum.FIELD, "fieldYearLessThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),
  /** The gteq year. */
  FIELD_GTEQ_YEAR(EntityTypeEnum.FIELD, "fieldYearGreaterThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),
  /** The lteq year. */
  FIELD_LTEQ_YEAR(EntityTypeEnum.FIELD, "fieldYearLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),

  // Date operators
  /** The eq date. */
  FIELD_EQ_DATE(EntityTypeEnum.FIELD, "fieldDateEquals", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.DATE),
  /** The dist date. */
  FIELD_DIST_DATE(EntityTypeEnum.FIELD, "fieldDateDistinct", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.DATE),
  /** The gt date. */
  FIELD_GT_DATE(EntityTypeEnum.FIELD, "fieldDateGreaterThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.DATE),
  /** The lt date. */
  FIELD_LT_DATE(EntityTypeEnum.FIELD, "fieldDateLessThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.DATE),
  /** The gteq date. */
  FIELD_GTEQ_DATE(EntityTypeEnum.FIELD, "fieldDateGreaterThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),
  /** The lteq date. */
  FIELD_LTEQ_DATE(EntityTypeEnum.FIELD, "fieldDateLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE);

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
