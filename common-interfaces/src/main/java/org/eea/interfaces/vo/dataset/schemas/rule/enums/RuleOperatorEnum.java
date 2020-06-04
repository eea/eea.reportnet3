package org.eea.interfaces.vo.dataset.schemas.rule.enums;

import java.util.HashMap;
import java.util.Map;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;

/** The Enum RuleOperatorEnum. */
public enum RuleOperatorEnum {

  // ***************************
  // ***** RECORD OPERATORS ****
  // ***************************

  // Logical operators

  /** The record if. */
  RECORD_IF(EntityTypeEnum.RECORD, "recordIfThen", JavaType.BOOLEAN, JavaType.BOOLEAN,
      JavaType.BOOLEAN),

  /** The record and. */
  RECORD_AND(EntityTypeEnum.RECORD, "recordAnd", JavaType.BOOLEAN, JavaType.BOOLEAN,
      JavaType.BOOLEAN),

  /** The record or. */
  RECORD_OR(EntityTypeEnum.RECORD, "recordOr", JavaType.BOOLEAN, JavaType.BOOLEAN,
      JavaType.BOOLEAN),

  /** The record not. */
  RECORD_NOT(EntityTypeEnum.RECORD, "recordNot", JavaType.BOOLEAN, JavaType.BOOLEAN),

  // Number operators

  /** The record eq. */
  RECORD_EQ(EntityTypeEnum.RECORD, "recordNumberEquals", JavaType.BOOLEAN, JavaType.NUMBER,
      JavaType.NUMBER),

  /** The record dist. */
  RECORD_DIST(EntityTypeEnum.RECORD, "recordNumberDistinct", JavaType.BOOLEAN, JavaType.NUMBER,
      JavaType.NUMBER),

  /** The record gt. */
  RECORD_GT(EntityTypeEnum.RECORD, "recordNumberGreaterThan", JavaType.BOOLEAN, JavaType.NUMBER,
      JavaType.NUMBER),

  /** The record lt. */
  RECORD_LT(EntityTypeEnum.RECORD, "recordNumberLessThan", JavaType.BOOLEAN, JavaType.NUMBER,
      JavaType.NUMBER),

  /** The record gteq. */
  RECORD_GTEQ(EntityTypeEnum.RECORD, "recordNumberGreaterThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.NUMBER, JavaType.NUMBER),

  /** The record lteq. */
  RECORD_LTEQ(EntityTypeEnum.RECORD, "recordNumberLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.NUMBER, JavaType.NUMBER),

  /** The record eq record. */
  RECORD_EQ_RECORD(EntityTypeEnum.RECORD, "recordNumberEqualsRecord", JavaType.BOOLEAN,
      JavaType.NUMBER, JavaType.NUMBER),

  /** The record dist record. */
  RECORD_DIST_RECORD(EntityTypeEnum.RECORD, "recordNumberDistinctRecord", JavaType.BOOLEAN,
      JavaType.NUMBER, JavaType.NUMBER),

  /** The record gt record. */
  RECORD_GT_RECORD(EntityTypeEnum.RECORD, "recordNumberGreaterThanRecord", JavaType.BOOLEAN,
      JavaType.NUMBER, JavaType.NUMBER),

  /** The record lt record. */
  RECORD_LT_RECORD(EntityTypeEnum.RECORD, "recordNumberLessThanRecord", JavaType.BOOLEAN,
      JavaType.NUMBER, JavaType.NUMBER),

  /** The record gteq record. */
  RECORD_GTEQ_RECORD(EntityTypeEnum.RECORD, "recordNumberGreaterThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.NUMBER, JavaType.NUMBER),

  /** The record lteq record. */
  RECORD_LTEQ_RECORD(EntityTypeEnum.RECORD, "recordNumberLessThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.NUMBER, JavaType.NUMBER),

  /** The record num match. */
  RECORD_NUM_MATCH(EntityTypeEnum.RECORD, "recordNumberMatches", JavaType.BOOLEAN, JavaType.NUMBER,
      JavaType.STRING),

  // String operators

  /** The record len. */
  RECORD_LEN(EntityTypeEnum.RECORD, "recordStringLength", JavaType.NUMBER, JavaType.STRING),

  /** The record len eq. */
  RECORD_LEN_EQ(EntityTypeEnum.RECORD, "recordStringLengthEquals", JavaType.BOOLEAN,
      JavaType.STRING, JavaType.NUMBER),

  /** The record len dist. */
  RECORD_LEN_DIST(EntityTypeEnum.RECORD, "recordStringLengthDistinct", JavaType.BOOLEAN,
      JavaType.STRING, JavaType.NUMBER),

  /** The record len gt. */
  RECORD_LEN_GT(EntityTypeEnum.RECORD, "recordStringLengthGreaterThan", JavaType.BOOLEAN,
      JavaType.STRING, JavaType.NUMBER),

  /** The record len lt. */
  RECORD_LEN_LT(EntityTypeEnum.RECORD, "recordStringLengthLessThan", JavaType.BOOLEAN,
      JavaType.STRING, JavaType.NUMBER),

  /** The record len gteq. */
  RECORD_LEN_GTEQ(EntityTypeEnum.RECORD, "recordStringLengthGreaterThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.STRING, JavaType.NUMBER),

  /** The record len lteq. */
  RECORD_LEN_LTEQ(EntityTypeEnum.RECORD, "recordStringLengthLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.STRING, JavaType.NUMBER),

  /** The record seq. */
  RECORD_SEQ(EntityTypeEnum.RECORD, "recordStringEquals", JavaType.BOOLEAN, JavaType.STRING,
      JavaType.STRING),

  /** The record seqic. */
  RECORD_SEQIC(EntityTypeEnum.RECORD, "recordStringEqualsIgnoreCase", JavaType.BOOLEAN,
      JavaType.STRING, JavaType.STRING),

  /** The record match. */
  RECORD_MATCH(EntityTypeEnum.RECORD, "recordStringMatches", JavaType.BOOLEAN, JavaType.STRING,
      JavaType.STRING),

  /** The record seq record. */
  RECORD_SEQ_RECORD(EntityTypeEnum.RECORD, "recordStringEqualsRecord", JavaType.BOOLEAN,
      JavaType.STRING, JavaType.STRING),

  /** The record seqic record. */
  RECORD_SEQIC_RECORD(EntityTypeEnum.RECORD, "recordStringEqualsIgnoreCaseRecord", JavaType.BOOLEAN,
      JavaType.STRING, JavaType.STRING),

  /** The record match record. */
  RECORD_MATCH_RECORD(EntityTypeEnum.RECORD, "recordStringMatchesRecord", JavaType.BOOLEAN,
      JavaType.STRING, JavaType.STRING),

  // Day operators

  /** The record eq day. */
  RECORD_EQ_DAY(EntityTypeEnum.RECORD, "recordDayEquals", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The record dist day. */
  RECORD_DIST_DAY(EntityTypeEnum.RECORD, "recordDayDistinct", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The record gt day. */
  RECORD_GT_DAY(EntityTypeEnum.RECORD, "recordDayGreaterThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The record lt day. */
  RECORD_LT_DAY(EntityTypeEnum.RECORD, "recordDayLessThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The record gteq day. */
  RECORD_GTEQ_DAY(EntityTypeEnum.RECORD, "recordDayGreaterThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),

  /** The record lteq day. */
  RECORD_LTEQ_DAY(EntityTypeEnum.RECORD, "recordDayLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),

  /** The record eq day record. */
  RECORD_EQ_DAY_RECORD(EntityTypeEnum.RECORD, "recordDayEqualsRecord", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record dist day record. */
  RECORD_DIST_DAY_RECORD(EntityTypeEnum.RECORD, "recordDayDistinctRecord", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record gt day record. */
  RECORD_GT_DAY_RECORD(EntityTypeEnum.RECORD, "recordDayGreaterThanRecord", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record lt day record. */
  RECORD_LT_DAY_RECORD(EntityTypeEnum.RECORD, "recordDayLessThanRecord", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record gteq day record. */
  RECORD_GTEQ_DAY_RECORD(EntityTypeEnum.RECORD, "recordDayGreaterThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.DATE),

  /** The record lteq day record. */
  RECORD_LTEQ_DAY_RECORD(EntityTypeEnum.RECORD, "recordDayLessThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.DATE),

  /** The record eq day record number. */
  RECORD_EQ_DAY_RECORD_NUMBER(EntityTypeEnum.RECORD, "recordDayEqualsRecordNumber",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.NUMBER),

  /** The record dist day record number. */
  RECORD_DIST_DAY_RECORD_NUMBER(EntityTypeEnum.RECORD, "recordDayDistinctRecordNumber",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.NUMBER),

  /** The record gt day record number. */
  RECORD_GT_DAY_RECORD_NUMBER(EntityTypeEnum.RECORD, "recordDayGreaterThanRecordNumber",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.NUMBER),

  /** The record lt day record number. */
  RECORD_LT_DAY_RECORD_NUMBER(EntityTypeEnum.RECORD, "recordDayLessThanRecordNumber",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.NUMBER),

  /** The record gteq day record number. */
  RECORD_GTEQ_DAY_RECORD_NUMBER(EntityTypeEnum.RECORD,
      "recordDayGreaterThanOrEqualsThanRecordNumber", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The record lteq day record number. */
  RECORD_LTEQ_DAY_RECORD_NUMBER(EntityTypeEnum.RECORD, "recordDayLessThanOrEqualsThanRecordNumber",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.NUMBER),

  // Month operators

  /** The record eq month. */
  RECORD_EQ_MONTH(EntityTypeEnum.RECORD, "recordMonthEquals", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The record dist month. */
  RECORD_DIST_MONTH(EntityTypeEnum.RECORD, "recordMonthDistinct", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The record gt month. */
  RECORD_GT_MONTH(EntityTypeEnum.RECORD, "recordMonthGreaterThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The record lt month. */
  RECORD_LT_MONTH(EntityTypeEnum.RECORD, "recordMonthLessThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The record gteq month. */
  RECORD_GTEQ_MONTH(EntityTypeEnum.RECORD, "recordMonthGreaterThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),

  /** The record lteq month. */
  RECORD_LTEQ_MONTH(EntityTypeEnum.RECORD, "recordMonthLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),

  /** The record eq month record. */
  RECORD_EQ_MONTH_RECORD(EntityTypeEnum.RECORD, "recordMonthEqualsRecord", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record dist month record. */
  RECORD_DIST_MONTH_RECORD(EntityTypeEnum.RECORD, "recordMonthDistinctRecord", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record gt month record. */
  RECORD_GT_MONTH_RECORD(EntityTypeEnum.RECORD, "recordMonthGreaterThanRecord", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record lt month record. */
  RECORD_LT_MONTH_RECORD(EntityTypeEnum.RECORD, "recordMonthLessThanRecord", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record gteq month record. */
  RECORD_GTEQ_MONTH_RECORD(EntityTypeEnum.RECORD, "recordMonthGreaterThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.DATE),

  /** The record lteq month record. */
  RECORD_LTEQ_MONTH_RECORD(EntityTypeEnum.RECORD, "recordMonthLessThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.DATE),

  /** The record eq month record number. */
  RECORD_EQ_MONTH_RECORD_NUMBER(EntityTypeEnum.RECORD, "recordMonthEqualsRecordNumber",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.NUMBER),

  /** The record dist month record number. */
  RECORD_DIST_MONTH_RECORD_NUMBER(EntityTypeEnum.RECORD, "recordMonthDistinctRecordNumber",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.NUMBER),

  /** The record gt month record number. */
  RECORD_GT_MONTH_RECORD_NUMBER(EntityTypeEnum.RECORD, "recordMonthGreaterThanRecordNumber",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.NUMBER),

  /** The record lt month record number. */
  RECORD_LT_MONTH_RECORD_NUMBER(EntityTypeEnum.RECORD, "recordMonthLessThanRecordNumber",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.NUMBER),

  /** The record gteq month record number. */
  RECORD_GTEQ_MONTH_RECORD_NUMBER(EntityTypeEnum.RECORD,
      "recordMonthGreaterThanOrEqualsThanRecordNumber", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The record lteq month record number. */
  RECORD_LTEQ_MONTH_RECORD_NUMBER(EntityTypeEnum.RECORD,
      "recordMonthLessThanOrEqualsThanRecordNumber", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  // Year operators

  /** The record eq year. */
  RECORD_EQ_YEAR(EntityTypeEnum.RECORD, "recordYearEquals", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The record dist year. */
  RECORD_DIST_YEAR(EntityTypeEnum.RECORD, "recordYearDistinct", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The record gt year. */
  RECORD_GT_YEAR(EntityTypeEnum.RECORD, "recordYearGreaterThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The record lt year. */
  RECORD_LT_YEAR(EntityTypeEnum.RECORD, "recordYearLessThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The record gteq year. */
  RECORD_GTEQ_YEAR(EntityTypeEnum.RECORD, "recordYearGreaterThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),

  /** The record lteq year. */
  RECORD_LTEQ_YEAR(EntityTypeEnum.RECORD, "recordYearLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),

  /** The record eq year record. */
  RECORD_EQ_YEAR_RECORD(EntityTypeEnum.RECORD, "recordYearEqualsRecord", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record dist year record. */
  RECORD_DIST_YEAR_RECORD(EntityTypeEnum.RECORD, "recordYearDistinctRecord", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record gt year record. */
  RECORD_GT_YEAR_RECORD(EntityTypeEnum.RECORD, "recordYearGreaterThanRecord", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record lt year record. */
  RECORD_LT_YEAR_RECORD(EntityTypeEnum.RECORD, "recordYearLessThanRecord", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record gteq year record. */
  RECORD_GTEQ_YEAR_RECORD(EntityTypeEnum.RECORD, "recordYearGreaterThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.DATE),

  /** The record lteq year record. */
  RECORD_LTEQ_YEAR_RECORD(EntityTypeEnum.RECORD, "recordYearLessThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.DATE),

  /** The record eq year record number. */
  RECORD_EQ_YEAR_RECORD_NUMBER(EntityTypeEnum.RECORD, "recordYearEqualsRecordNumber",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.NUMBER),

  /** The record dist year record number. */
  RECORD_DIST_YEAR_RECORD_NUMBER(EntityTypeEnum.RECORD, "recordYearDistinctRecordNumber",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.NUMBER),

  /** The record gt year record number. */
  RECORD_GT_YEAR_RECORD_NUMBER(EntityTypeEnum.RECORD, "recordYearGreaterThanRecordNumber",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.NUMBER),

  /** The record lt year record number. */
  RECORD_LT_YEAR_RECORD_NUMBER(EntityTypeEnum.RECORD, "recordYearLessThanRecordNumber",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.NUMBER),

  /** The record gteq year record number. */
  RECORD_GTEQ_YEAR_RECORD_NUMBER(EntityTypeEnum.RECORD,
      "recordYearGreaterThanOrEqualsThanRecordNumber", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The record lteq year record number. */
  RECORD_LTEQ_YEAR_RECORD_NUMBER(EntityTypeEnum.RECORD,
      "recordYearLessThanOrEqualsThanRecordNumber", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  // Date operators

  /** The record eq date. */
  RECORD_EQ_DATE(EntityTypeEnum.RECORD, "recordDateEquals", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.DATE),

  /** The record dist date. */
  RECORD_DIST_DATE(EntityTypeEnum.RECORD, "recordDateDistinct", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.DATE),

  /** The record gt date. */
  RECORD_GT_DATE(EntityTypeEnum.RECORD, "recordDateGreaterThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.DATE),

  /** The record lt date. */
  RECORD_LT_DATE(EntityTypeEnum.RECORD, "recordDateLessThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.DATE),

  /** The record gteq date. */
  RECORD_GTEQ_DATE(EntityTypeEnum.RECORD, "recordDateGreaterThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record lteq date. */
  RECORD_LTEQ_DATE(EntityTypeEnum.RECORD, "recordDateLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record eq date record. */
  RECORD_EQ_DATE_RECORD(EntityTypeEnum.RECORD, "recordDateEqualsRecord", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record dist date record. */
  RECORD_DIST_DATE_RECORD(EntityTypeEnum.RECORD, "recordDateDistinctRecord", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record gt date record. */
  RECORD_GT_DATE_RECORD(EntityTypeEnum.RECORD, "recordDateGreaterThanRecord", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record lt date record. */
  RECORD_LT_DATE_RECORD(EntityTypeEnum.RECORD, "recordDateLessThanRecord", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The record gteq date record. */
  RECORD_GTEQ_DATE_RECORD(EntityTypeEnum.RECORD, "recordDateGreaterThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.DATE),

  /** The record lteq date record. */
  RECORD_LTEQ_DATE_RECORD(EntityTypeEnum.RECORD, "recordDateLessThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.DATE),

  // ***************************
  // ***** FIELD OPERATORS *****
  // ***************************

  // Logical operators

  /** The field and. */
  FIELD_AND(EntityTypeEnum.FIELD, "fieldAnd", JavaType.BOOLEAN, JavaType.BOOLEAN, JavaType.BOOLEAN),

  /** The field or. */
  FIELD_OR(EntityTypeEnum.FIELD, "fieldOr", JavaType.BOOLEAN, JavaType.BOOLEAN, JavaType.BOOLEAN),

  /** The field not. */
  FIELD_NOT(EntityTypeEnum.FIELD, "fieldNot", JavaType.BOOLEAN, JavaType.BOOLEAN),

  // Number operators

  /** The field eq. */
  FIELD_EQ(EntityTypeEnum.FIELD, "fieldNumberEquals", JavaType.BOOLEAN, JavaType.NUMBER,
      JavaType.NUMBER),

  /** The field dist. */
  FIELD_DIST(EntityTypeEnum.FIELD, "fieldNumberDistinct", JavaType.BOOLEAN, JavaType.NUMBER,
      JavaType.NUMBER),

  /** The field gt. */
  FIELD_GT(EntityTypeEnum.FIELD, "fieldNumberGreaterThan", JavaType.BOOLEAN, JavaType.NUMBER,
      JavaType.NUMBER),

  /** The field lt. */
  FIELD_LT(EntityTypeEnum.FIELD, "fieldNumberLessThan", JavaType.BOOLEAN, JavaType.NUMBER,
      JavaType.NUMBER),

  /** The field gteq. */
  FIELD_GTEQ(EntityTypeEnum.FIELD, "fieldNumberGreaterThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.NUMBER, JavaType.NUMBER),

  /** The field lteq. */
  FIELD_LTEQ(EntityTypeEnum.FIELD, "fieldNumberLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.NUMBER, JavaType.NUMBER),

  /** The field num match. */
  FIELD_NUM_MATCH(EntityTypeEnum.FIELD, "fieldNumberMatches", JavaType.BOOLEAN, JavaType.NUMBER,
      JavaType.STRING),

  // String operators

  /** The field len. */
  FIELD_LEN(EntityTypeEnum.FIELD, "fieldStringLength", JavaType.NUMBER, JavaType.STRING),

  /** The field seq. */
  FIELD_SEQ(EntityTypeEnum.FIELD, "fieldStringEquals", JavaType.BOOLEAN, JavaType.STRING,
      JavaType.STRING),

  /** The field seqic. */
  FIELD_SEQIC(EntityTypeEnum.FIELD, "fieldStringEqualsIgnoreCase", JavaType.BOOLEAN,
      JavaType.STRING, JavaType.STRING),

  /** The field match. */
  FIELD_MATCH(EntityTypeEnum.FIELD, "fieldStringMatches", JavaType.BOOLEAN, JavaType.STRING,
      JavaType.STRING),

  // Day operators

  /** The field eq day. */
  FIELD_EQ_DAY(EntityTypeEnum.FIELD, "fieldDayEquals", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The field dist day. */
  FIELD_DIST_DAY(EntityTypeEnum.FIELD, "fieldDayDistinct", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The field gt day. */
  FIELD_GT_DAY(EntityTypeEnum.FIELD, "fieldDayGreaterThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The field lt day. */
  FIELD_LT_DAY(EntityTypeEnum.FIELD, "fieldDayLessThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The field gteq day. */
  FIELD_GTEQ_DAY(EntityTypeEnum.FIELD, "fieldDayGreaterThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),

  /** The field lteq day. */
  FIELD_LTEQ_DAY(EntityTypeEnum.FIELD, "fieldDayLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),

  // Month operators

  /** The field eq month. */
  FIELD_EQ_MONTH(EntityTypeEnum.FIELD, "fieldMonthEquals", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The field dist month. */
  FIELD_DIST_MONTH(EntityTypeEnum.FIELD, "fieldMonthDistinct", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The field gt month. */
  FIELD_GT_MONTH(EntityTypeEnum.FIELD, "fieldMonthGreaterThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The field lt month. */
  FIELD_LT_MONTH(EntityTypeEnum.FIELD, "fieldMonthLessThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The field gteq month. */
  FIELD_GTEQ_MONTH(EntityTypeEnum.FIELD, "fieldMonthGreaterThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),

  /** The field lteq month. */
  FIELD_LTEQ_MONTH(EntityTypeEnum.FIELD, "fieldMonthLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),

  // Year operators

  /** The field eq year. */
  FIELD_EQ_YEAR(EntityTypeEnum.FIELD, "fieldYearEquals", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The field dist year. */
  FIELD_DIST_YEAR(EntityTypeEnum.FIELD, "fieldYearDistinct", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The field gt year. */
  FIELD_GT_YEAR(EntityTypeEnum.FIELD, "fieldYearGreaterThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The field lt year. */
  FIELD_LT_YEAR(EntityTypeEnum.FIELD, "fieldYearLessThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.NUMBER),

  /** The field gteq year. */
  FIELD_GTEQ_YEAR(EntityTypeEnum.FIELD, "fieldYearGreaterThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),

  /** The field lteq year. */
  FIELD_LTEQ_YEAR(EntityTypeEnum.FIELD, "fieldYearLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.NUMBER),

  // Date operators

  /** The field eq date. */
  FIELD_EQ_DATE(EntityTypeEnum.FIELD, "fieldDateEquals", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.DATE),

  /** The field dist date. */
  FIELD_DIST_DATE(EntityTypeEnum.FIELD, "fieldDateDistinct", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.DATE),

  /** The field gt date. */
  FIELD_GT_DATE(EntityTypeEnum.FIELD, "fieldDateGreaterThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.DATE),

  /** The field lt date. */
  FIELD_LT_DATE(EntityTypeEnum.FIELD, "fieldDateLessThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.DATE),

  /** The field gteq date. */
  FIELD_GTEQ_DATE(EntityTypeEnum.FIELD, "fieldDateGreaterThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE),

  /** The field lteq date. */
  FIELD_LTEQ_DATE(EntityTypeEnum.FIELD, "fieldDateLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.DATE);

  /** The entity type. */
  private final EntityTypeEnum entityType;

  /** The function name. */
  private final String functionName;

  /** The return type. */
  private final String returnType;

  /** The param types. */
  private final String[] paramTypes;

  /** The Constant map. */
  private static final Map<String, RuleOperatorEnum> map;

  static {
    map = new HashMap<>();
    for (RuleOperatorEnum e : values()) {
      map.put(e.getFunctionName(), e);
    }
  }

  /**
   * Instantiates a new rule operator enum.
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
   * Value of function name.
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
