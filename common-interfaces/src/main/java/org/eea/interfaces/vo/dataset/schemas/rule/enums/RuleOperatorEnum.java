package org.eea.interfaces.vo.dataset.schemas.rule.enums;

import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Enum RuleOperatorEnum.
 */
public enum RuleOperatorEnum {

  // Number operators
  /** Equals. */
  EQ("=="),
  /** Distinct. */
  DIST("!="),
  /** Greater than. */
  GT(">"),
  /** Less than. */
  LT("<"),
  /** Greater than or equals. */
  GTEQ(">="),
  /** Less than or equals. */
  LTEQ("<="),

  // Logical operators
  /** And. */
  AND("&&"),
  /** Or. */
  OR("||"),
  /** Not. */
  NOT("!"),

  // String operators
  /** Length. */
  LEN("length"),
  /** Equals for strings. */
  SEQ("equals"),
  /** Equals for string ignoring case. */
  SEQIC("equalsIgnoreCase"),
  /** Match. */
  MATCH("matches"),

  // Date operators
  /** The eq date. */
  EQ_DATE("equalDate"),
  /** The dist date. */
  DIST_DATE("distinctDate"),
  /** The gt date. */
  GT_DATE("greaterThanDate"),
  /** The lt date. */
  LT_DATE("lessThanDate"),
  /** The gteq date. */
  GTEQ_DATE("greaterThanOrEqualsThanDate"),
  /** The lteq date. */
  LTEQ_DATE("lessThanOrEqualsThanDate");

  /** Operator's Java representation. */
  private final String label;

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
