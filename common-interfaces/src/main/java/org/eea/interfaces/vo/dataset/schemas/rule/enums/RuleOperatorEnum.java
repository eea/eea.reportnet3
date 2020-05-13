package org.eea.interfaces.vo.dataset.schemas.rule.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * The Enum RuleOperatorEnum.
 */
public enum RuleOperatorEnum {

  // Number operators
  /** Equals. */
  EQ("==", "Boolean", "Number", "Number"),
  /** Distinct. */
  DIST("!=", "Boolean", "Number", "Number"),
  /** Greater than. */
  GT(">", "Boolean", "Number", "Number"),
  /** Less than. */
  LT("<", "Boolean", "Number", "Number"),
  /** Greater than or equals. */
  GTEQ(">=", "Boolean", "Number", "Number"),
  /** Less than or equals. */
  LTEQ("<=", "Boolean", "Number", "Number"),

  // Logical operators
  /** And. */
  AND("&&", "Boolean", "Boolean", "Boolean"),
  /** Or. */
  OR("||", "Boolean", "Boolean", "Boolean"),
  /** Not. */
  NOT("!", "Boolean", "Boolean", "Boolean"),

  // String operators
  /** Length. */
  LEN("length", "Number", "String"),
  /** Equals for strings. */
  SEQ("equals", "Boolean", "String", "String"),
  /** Equals for string ignoring case. */
  SEQIC("equalsIgnoreCase", "Boolean", "String", "String"),
  /** Match. */
  MATCH("matches", "Boolean", "String", "String"),

  // Day operators
  /** The eq day. */
  EQ_DAY("equalDay", "Boolean", "Date", "Number"),
  /** The dist day. */
  DIST_DAY("distinctDay", "Boolean", "Date", "Number"),
  /** The gt day. */
  GT_DAY("greaterThanDay", "Boolean", "Date", "Number"),
  /** The lt day. */
  LT_DAY("lessThanDay", "Boolean", "Date", "Number"),
  /** The gteq day. */
  GTEQ_DAY("greaterThanOrEqualsThanDay", "Boolean", "Date", "Number"),
  /** The lteq day. */
  LTEQ_DAY("lessThanOrEqualsThanDay", "Boolean", "Date", "Number"),

  // Month operators
  /** The eq month. */
  EQ_MONTH("equalMonth", "Boolean", "Date", "Number"),
  /** The dist month. */
  DIST_MONTH("distinctMonth", "Boolean", "Date", "Number"),
  /** The gt month. */
  GT_MONTH("greaterThanMonth", "Boolean", "Date", "Number"),
  /** The lt month. */
  LT_MONTH("lessThanMonth", "Boolean", "Date", "Number"),
  /** The gteq month. */
  GTEQ_MONTH("greaterThanOrEqualsThanMonth", "Boolean", "Date", "Number"),
  /** The lteq month. */
  LTEQ_MONTH("lessThanOrEqualsThanMonth", "Boolean", "Date", "Number"),

  // Year operators
  /** The eq year. */
  EQ_YEAR("equalYear", "Boolean", "Date", "Number"),
  /** The dist year. */
  DIST_YEAR("distinctYear", "Boolean", "Date", "Number"),
  /** The gt year. */
  GT_YEAR("greaterThanYear", "Boolean", "Date", "Number"),
  /** The lt year. */
  LT_YEAR("lessThanYear", "Boolean", "Date", "Number"),
  /** The gteq year. */
  GTEQ_YEAR("greaterThanOrEqualsThanYear", "Boolean", "Date", "Number"),
  /** The lteq year. */
  LTEQ_YEAR("lessThanOrEqualsThanYear", "Boolean", "Date", "Number"),

  // Date operators
  /** The eq date. */
  EQ_DATE("equalDate", "Boolean", "Date", "Date"),
  /** The dist date. */
  DIST_DATE("distinctDate", "Boolean", "Date", "Date"),
  /** The gt date. */
  GT_DATE("greaterThanDate", "Boolean", "Date", "Date"),
  /** The lt date. */
  LT_DATE("lessThanDate", "Boolean", "Date", "Date"),
  /** The gteq date. */
  GTEQ_DATE("greaterThanOrEqualsThanDate", "Boolean", "Date", "Date"),
  /** The lteq date. */
  LTEQ_DATE("lessThanOrEqualsThanDate", "Boolean", "Date", "Date");

  /** Operator's Java representation. */
  private final String label;

  /** The return type. */
  private final String returnType;

  /** The input types. */
  private final String[] inputTypes;

  /** Transformation between RuleOperatorEnum and Java representation. */
  private static final Map<String, RuleOperatorEnum> map;

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
   * @param returnType the return type
   * @param inputTypes the input types
   */
  private RuleOperatorEnum(String label, String returnType, String... inputTypes) {
    this.label = label;
    this.returnType = returnType;
    this.inputTypes = inputTypes;
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

  /**
   * Gets the return type.
   *
   * @return the return type
   */
  public String getReturnType() {
    return returnType;
  }

  /**
   * Gets the input types.
   *
   * @return the input types
   */
  public String[] getInputTypes() {
    return inputTypes;
  }
}
