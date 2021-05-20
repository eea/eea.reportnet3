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

  /** The record null. */
  RECORD_NULL(EntityTypeEnum.RECORD, "recordNull", JavaType.BOOLEAN, JavaType.OBJECT),

  /** The record not null. */
  RECORD_NOT_NULL(EntityTypeEnum.RECORD, "recordNotNull", JavaType.BOOLEAN, JavaType.OBJECT),

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

  /** The record len eq record. */
  RECORD_LEN_EQ_RECORD(EntityTypeEnum.RECORD, "recordStringLengthEqualsRecord", JavaType.BOOLEAN,
      JavaType.STRING, JavaType.NUMBER),

  /** The record len dist record. */
  RECORD_LEN_DIST_RECORD(EntityTypeEnum.RECORD, "recordStringLengthDistinctRecord",
      JavaType.BOOLEAN, JavaType.STRING, JavaType.NUMBER),

  /** The record len gt record. */
  RECORD_LEN_GT_RECORD(EntityTypeEnum.RECORD, "recordStringLengthGreaterThanRecord",
      JavaType.BOOLEAN, JavaType.STRING, JavaType.NUMBER),

  /** The record len lt record. */
  RECORD_LEN_LT_RECORD(EntityTypeEnum.RECORD, "recordStringLengthLessThanRecord", JavaType.BOOLEAN,
      JavaType.STRING, JavaType.NUMBER),

  /** The record len gteq record. */
  RECORD_LEN_GTEQ_RECORD(EntityTypeEnum.RECORD, "recordStringLengthGreaterThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.STRING, JavaType.NUMBER),

  /** The record len lteq record. */
  RECORD_LEN_LTEQ_RECORD(EntityTypeEnum.RECORD, "recordStringLengthLessThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.STRING, JavaType.NUMBER),

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

  /** The record eq day datetime. */
  RECORD_EQ_DAY_DATETIME(EntityTypeEnum.RECORD, "recordDaytimeEquals", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record dist day datetime. */
  RECORD_DIST_DAY_DATETIME(EntityTypeEnum.RECORD, "recordDaytimeDistinct", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record gt day datetime. */
  RECORD_GT_DAY_DATETIME(EntityTypeEnum.RECORD, "recordDaytimeGreaterThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record lt day datetime. */
  RECORD_LT_DAY_DATETIME(EntityTypeEnum.RECORD, "recordDaytimeLessThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record gteq day datetime. */
  RECORD_GTEQ_DAY_DATETIME(EntityTypeEnum.RECORD, "recordDaytimeGreaterThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record lteq day datetime. */
  RECORD_LTEQ_DAY_DATETIME(EntityTypeEnum.RECORD, "recordDaytimeLessThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record eq day record datetime. */
  RECORD_EQ_DAY_RECORD_DATETIME(EntityTypeEnum.RECORD, "recordDaytimeEqualsRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record dist day record datetime. */
  RECORD_DIST_DAY_RECORD_DATETIME(EntityTypeEnum.RECORD, "recordDaytimeDistinctRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record gt day record datetime. */
  RECORD_GT_DAY_RECORD_DATETIME(EntityTypeEnum.RECORD, "recordDaytimeGreaterThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record lt day record datetime. */
  RECORD_LT_DAY_RECORD_DATETIME(EntityTypeEnum.RECORD, "recordDaytimeLessThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record gteq day record datetime. */
  RECORD_GTEQ_DAY_RECORD_DATETIME(EntityTypeEnum.RECORD,
      "recordDaytimeGreaterThanOrEqualsThanRecord", JavaType.BOOLEAN, JavaType.TIMESTAMP,
      JavaType.TIMESTAMP),

  /** The record lteq day record datetime. */
  RECORD_LTEQ_DAY_RECORD_DATETIME(EntityTypeEnum.RECORD, "recordDaytimeLessThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record eq day record date datetime. */
  RECORD_EQ_DAY_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD, "recordDayEqualsRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record dist day record date datetime. */
  RECORD_DIST_DAY_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD, "recordDayDistinctRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record gt day record date datetime. */
  RECORD_GT_DAY_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD, "recordDayGreaterThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record lt day record date datetime. */
  RECORD_LT_DAY_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD, "recordDayLessThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record gteq day record date datetime. */
  RECORD_GTEQ_DAY_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD,
      "recordDayGreaterThanOrEqualsThanRecord", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.TIMESTAMP),

  /** The record lteq day record date datetime. */
  RECORD_LTEQ_DAY_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD, "recordDayLessThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record eq day record datetime date. */
  RECORD_EQ_DAY_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD, "recordDayEqualsRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record dist day record datetime date. */
  RECORD_DIST_DAY_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD, "recordDayDistinctRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record gt day record datetime date. */
  RECORD_GT_DAY_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD, "recordDayGreaterThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record lt day record datetime date. */
  RECORD_LT_DAY_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD, "recordDayLessThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record gteq day record datetime date. */
  RECORD_GTEQ_DAY_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD,
      "recordDayGreaterThanOrEqualsThanRecord", JavaType.BOOLEAN, JavaType.TIMESTAMP,
      JavaType.DATE),

  /** The record lteq day record datetime date. */
  RECORD_LTEQ_DAY_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD, "recordDayLessThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record eq day record number datetime. */
  RECORD_EQ_DAY_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD, "recordDaytimeEqualsRecordNumber",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record dist day record number datetime. */
  RECORD_DIST_DAY_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD, "recordDaytimeDistinctRecordNumber",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record gt day record number datetime. */
  RECORD_GT_DAY_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD,
      "recordDaytimeGreaterThanRecordNumber", JavaType.BOOLEAN, JavaType.TIMESTAMP,
      JavaType.NUMBER),

  /** The record lt day record number datetime. */
  RECORD_LT_DAY_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD, "recordDaytimeLessThanRecordNumber",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record gteq day record number datetime. */
  RECORD_GTEQ_DAY_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD,
      "recordDaytimeGreaterThanOrEqualsThanRecordNumber", JavaType.BOOLEAN, JavaType.TIMESTAMP,
      JavaType.NUMBER),

  /** The record lteq day record number datetime. */
  RECORD_LTEQ_DAY_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD,
      "recordDaytimeLessThanOrEqualsThanRecordNumber", JavaType.BOOLEAN, JavaType.TIMESTAMP,
      JavaType.NUMBER),

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

  /** The record eq month datetime. */
  RECORD_EQ_MONTH_DATETIME(EntityTypeEnum.RECORD, "recordMonthEquals", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record dist month datetime. */
  RECORD_DIST_MONTH_DATETIME(EntityTypeEnum.RECORD, "recordMonthtimeDistinct", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record gt month datetime. */
  RECORD_GT_MONTH_DATETIME(EntityTypeEnum.RECORD, "recordMonthtimeGreaterThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record lt month datetime. */
  RECORD_LT_MONTH_DATETIME(EntityTypeEnum.RECORD, "recordMonthtimeLessThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record gteq month datetime. */
  RECORD_GTEQ_MONTH_DATETIME(EntityTypeEnum.RECORD, "recordMonthtimeGreaterThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record lteq month datetime. */
  RECORD_LTEQ_MONTH_DATETIME(EntityTypeEnum.RECORD, "recordMonthtimeLessThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record eq month record datetime. */
  RECORD_EQ_MONTH_RECORD_DATETIME(EntityTypeEnum.RECORD, "recordMonthtimeEqualsRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record dist month record datetime. */
  RECORD_DIST_MONTH_RECORD_DATETIME(EntityTypeEnum.RECORD, "recordMonthtimeDistinctRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record gt month record datetime. */
  RECORD_GT_MONTH_RECORD_DATETIME(EntityTypeEnum.RECORD, "recordMonthtimeGreaterThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record lt month record datetime. */
  RECORD_LT_MONTH_RECORD_DATETIME(EntityTypeEnum.RECORD, "recordMonthtimeLessThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record gteq month record datetime. */
  RECORD_GTEQ_MONTH_RECORD_DATETIME(EntityTypeEnum.RECORD,
      "recordMonthtimeGreaterThanOrEqualsThanRecord", JavaType.BOOLEAN, JavaType.TIMESTAMP,
      JavaType.TIMESTAMP),

  /** The record lteq month record datetime. */
  RECORD_LTEQ_MONTH_RECORD_DATETIME(EntityTypeEnum.RECORD,
      "recordMonthtimeLessThanOrEqualsThanRecord", JavaType.BOOLEAN, JavaType.TIMESTAMP,
      JavaType.TIMESTAMP),

  /** The record eq month record datetime date. */
  RECORD_EQ_MONTH_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD, "recordMonthEqualsRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record dist month record datetime date. */
  RECORD_DIST_MONTH_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD, "recordMonthDistinctRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record gt month record datetime date. */
  RECORD_GT_MONTH_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD, "recordMonthGreaterThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record lt month record datetime date. */
  RECORD_LT_MONTH_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD, "recordMonthLessThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record gteq month record datetime date. */
  RECORD_GTEQ_MONTH_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD,
      "recordMonthGreaterThanOrEqualsThanRecord", JavaType.BOOLEAN, JavaType.TIMESTAMP,
      JavaType.DATE),

  /** The record lteq month record datetime date. */
  RECORD_LTEQ_MONTH_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD,
      "recordMonthLessThanOrEqualsThanRecord", JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record eq month record date datetime. */
  RECORD_EQ_MONTH_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD, "recordMonthEqualsRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record dist month record date datetime. */
  RECORD_DIST_MONTH_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD, "recordMonthDistinctRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record gt month record date datetime. */
  RECORD_GT_MONTH_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD, "recordMonthGreaterThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record lt month record date datetime. */
  RECORD_LT_MONTH_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD, "recordMonthLessThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record gteq month record date datetime. */
  RECORD_GTEQ_MONTH_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD,
      "recordMonthGreaterThanOrEqualsThanRecord", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.TIMESTAMP),

  /** The record lteq month record date datetime. */
  RECORD_LTEQ_MONTH_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD,
      "recordMonthLessThanOrEqualsThanRecord", JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record eq month record number datetime. */
  RECORD_EQ_MONTH_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD, "recordMonthtimeEqualsRecordNumber",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record dist month record number datetime. */
  RECORD_DIST_MONTH_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD,
      "recordMonthtimeDistinctRecordNumber", JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record gt month record number datetime. */
  RECORD_GT_MONTH_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD,
      "recordMonthtimeGreaterThanRecordNumber", JavaType.BOOLEAN, JavaType.TIMESTAMP,
      JavaType.NUMBER),

  /** The record lt month record number datetime. */
  RECORD_LT_MONTH_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD,
      "recordMonthtimeLessThanRecordNumber", JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record gteq month record number datetime. */
  RECORD_GTEQ_MONTH_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD,
      "recordMonthtimeGreaterThanOrEqualsThanRecordNumber", JavaType.BOOLEAN, JavaType.TIMESTAMP,
      JavaType.NUMBER),

  /** The record lteq month record number datetime. */
  RECORD_LTEQ_MONTH_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD,
      "recordMonthtimeLessThanOrEqualsThanRecordNumber", JavaType.BOOLEAN, JavaType.TIMESTAMP,
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

  /** The record eq year datetime. */
  RECORD_EQ_YEAR_DATETIME(EntityTypeEnum.RECORD, "recordYeartimeEquals", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record dist year datetime. */
  RECORD_DIST_YEAR_DATETIME(EntityTypeEnum.RECORD, "recordYeartimeDistinct", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record gt year datetime. */
  RECORD_GT_YEAR_DATETIME(EntityTypeEnum.RECORD, "recordYeartimeGreaterThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record lt year datetime. */
  RECORD_LT_YEAR_DATETIME(EntityTypeEnum.RECORD, "recordYeartimeLessThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record gteq year datetime. */
  RECORD_GTEQ_YEAR_DATETIME(EntityTypeEnum.RECORD, "recordYeartimeGreaterThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record lteq year datetime. */
  RECORD_LTEQ_YEAR_DATETIME(EntityTypeEnum.RECORD, "recordYeartimeLessThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record eq year record datetime. */
  RECORD_EQ_YEAR_RECORD_DATETIME(EntityTypeEnum.RECORD, "recordYeartimeEqualsRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record dist year record datetime. */
  RECORD_DIST_YEAR_RECORD_DATETIME(EntityTypeEnum.RECORD, "recordYeartimeDistinctRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record gt year record datetime. */
  RECORD_GT_YEAR_RECORD_DATETIME(EntityTypeEnum.RECORD, "recordYeartimeGreaterThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record lt year record datetime. */
  RECORD_LT_YEAR_RECORD_DATETIME(EntityTypeEnum.RECORD, "recordYeartimeLessThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record gteq year record datetime. */
  RECORD_GTEQ_YEAR_RECORD_DATETIME(EntityTypeEnum.RECORD,
      "recordYeartimeGreaterThanOrEqualsThanRecord", JavaType.BOOLEAN, JavaType.TIMESTAMP,
      JavaType.TIMESTAMP),

  /** The record lteq year record datetime. */
  RECORD_LTEQ_YEAR_RECORD_DATETIME(EntityTypeEnum.RECORD,
      "recordYeartimeLessThanOrEqualsThanRecord", JavaType.BOOLEAN, JavaType.TIMESTAMP,
      JavaType.TIMESTAMP),

  /** The record eq year record datetime date. */
  RECORD_EQ_YEAR_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD, "recordYearEqualsRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record dist year record datetime date. */
  RECORD_DIST_YEAR_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD, "recordYearDistinctRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record gt year record datetime date. */
  RECORD_GT_YEAR_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD, "recordYearGreaterThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record lt year record datetime date. */
  RECORD_LT_YEAR_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD, "recordYearLessThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record gteq year record datetime date. */
  RECORD_GTEQ_YEAR_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD,
      "recordYearGreaterThanOrEqualsThanRecord", JavaType.BOOLEAN, JavaType.TIMESTAMP,
      JavaType.DATE),

  /** The record lteq year record datetime date. */
  RECORD_LTEQ_YEAR_RECORD_DATETIME_DATE(EntityTypeEnum.RECORD,
      "recordYearLessThanOrEqualsThanRecord", JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record eq year record date datetime. */
  RECORD_EQ_YEAR_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD, "recordYearEqualsRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record dist year record date datetime. */
  RECORD_DIST_YEAR_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD, "recordYearDistinctRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record gt year record date datetime. */
  RECORD_GT_YEAR_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD, "recordYearGreaterThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record lt year record date datetime. */
  RECORD_LT_YEAR_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD, "recordYearLessThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record gteq year record date datetime. */
  RECORD_GTEQ_YEAR_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD,
      "recordYearGreaterThanOrEqualsThanRecord", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.TIMESTAMP),

  /** The record lteq year record date datetime. */
  RECORD_LTEQ_YEAR_RECORD_DATE_DATETIME(EntityTypeEnum.RECORD,
      "recordYearLessThanOrEqualsThanRecord", JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record eq year record number datetime. */
  RECORD_EQ_YEAR_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD, "recordYeartimeEqualsRecordNumber",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record dist year record number datetime. */
  RECORD_DIST_YEAR_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD,
      "recordYeartimeDistinctRecordNumber", JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record gt year record number datetime. */
  RECORD_GT_YEAR_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD,
      "recordYeartimeGreaterThanRecordNumber", JavaType.BOOLEAN, JavaType.TIMESTAMP,
      JavaType.NUMBER),

  /** The record lt year record number datetime. */
  RECORD_LT_YEAR_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD, "recordYeartimeLessThanRecordNumber",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The record gteq year record number datetime. */
  RECORD_GTEQ_YEAR_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD,
      "recordYeartimeGreaterThanOrEqualsThanRecordNumber", JavaType.BOOLEAN, JavaType.TIMESTAMP,
      JavaType.NUMBER),

  /** The record lteq year record number datetime. */
  RECORD_LTEQ_YEAR_RECORD_NUMBER_DATETIME(EntityTypeEnum.RECORD,
      "recordYeartimeLessThanOrEqualsThanRecordNumber", JavaType.BOOLEAN, JavaType.TIMESTAMP,
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

  // DATETIME operators

  /** The record eq datetime. */
  RECORD_EQ_DATETIME(EntityTypeEnum.RECORD, "recordDatetimeEquals", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record dist datetime. */
  RECORD_DIST_DATETIME(EntityTypeEnum.RECORD, "recordDatetimeDistinct", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record gt datetime. */
  RECORD_GT_DATETIME(EntityTypeEnum.RECORD, "recordDatetimeGreaterThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record lt datetime. */
  RECORD_LT_DATETIME(EntityTypeEnum.RECORD, "recordDatetimeLessThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record gteq datetime. */
  RECORD_GTEQ_DATETIME(EntityTypeEnum.RECORD, "recordDatetimeGreaterThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record lteq datetime. */
  RECORD_LTEQ_DATETIME(EntityTypeEnum.RECORD, "recordDatetimeLessThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record eq datetime record. */
  RECORD_EQ_DATETIME_RECORD(EntityTypeEnum.RECORD, "recordDatetimeEqualsRecord", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record dist datetime record. */
  RECORD_DIST_DATETIME_RECORD(EntityTypeEnum.RECORD, "recordDatetimeDistinctRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record gt datetime record. */
  RECORD_GT_DATETIME_RECORD(EntityTypeEnum.RECORD, "recordDatetimeGreaterThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record lt datetime record. */
  RECORD_LT_DATETIME_RECORD(EntityTypeEnum.RECORD, "recordDatetimeLessThanRecord", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record gteq datetime record. */
  RECORD_GTEQ_DATETIME_RECORD(EntityTypeEnum.RECORD, "recordDatetimeGreaterThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The record lteq datetime record. */
  RECORD_LTEQ_DATETIME_RECORD(EntityTypeEnum.RECORD, "recordDatetimeLessThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  // Date to datetime operators

  /** The record eq date datetime. */
  RECORD_EQ_DATE_DATETIME(EntityTypeEnum.RECORD, "recordDateEquals", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.TIMESTAMP),

  /** The record dist date datetime. */
  RECORD_DIST_DATE_DATETIME(EntityTypeEnum.RECORD, "recordDateDistinct", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.TIMESTAMP),

  /** The record gt date datetime. */
  RECORD_GT_DATE_DATETIME(EntityTypeEnum.RECORD, "recordDateGreaterThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.TIMESTAMP),

  /** The record lt date datetime. */
  RECORD_LT_DATE_DATETIME(EntityTypeEnum.RECORD, "recordDateLessThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.TIMESTAMP),

  /** The record gteq date datetime. */
  RECORD_GTEQ_DATE_DATETIME(EntityTypeEnum.RECORD, "recordDateGreaterThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record lteq date datetime. */
  RECORD_LTEQ_DATE_DATETIME(EntityTypeEnum.RECORD, "recordDateLessThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record eq date datetime record. */
  RECORD_EQ_DATE_DATETIME_RECORD(EntityTypeEnum.RECORD, "recordDateEqualsRecord", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.TIMESTAMP),

  /** The record dist date datetime record. */
  RECORD_DIST_DATE_DATETIME_RECORD(EntityTypeEnum.RECORD, "recordDateDistinctRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record gt date datetime record. */
  RECORD_GT_DATE_DATETIME_RECORD(EntityTypeEnum.RECORD, "recordDateGreaterThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record lt date datetime record. */
  RECORD_LT_DATE_DATETIME_RECORD(EntityTypeEnum.RECORD, "recordDateLessThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record gteq date datetime record. */
  RECORD_GTEQ_DATE_DATETIME_RECORD(EntityTypeEnum.RECORD, "recordDateGreaterThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The record lteq date datetime record. */
  RECORD_LTEQ_DATE_DATETIME_RECORD(EntityTypeEnum.RECORD, "recordDateLessThanOrEqualsThanRecord",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  // Datetime to date operators

  /** The record eq datetime date. */
  RECORD_EQ_DATETIME_DATE(EntityTypeEnum.RECORD, "recordDateEquals", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.DATE),

  /** The record dist datetime date. */
  RECORD_DIST_DATETIME_DATE(EntityTypeEnum.RECORD, "recordDateDistinct", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.DATE),

  /** The record gt datetime date. */
  RECORD_GT_DATETIME_DATE(EntityTypeEnum.RECORD, "recordDateGreaterThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.DATE),

  /** The record lt datetime date. */
  RECORD_LT_DATETIME_DATE(EntityTypeEnum.RECORD, "recordDateLessThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.DATE),

  /** The record gteq datetime date. */
  RECORD_GTEQ_DATETIME_DATE(EntityTypeEnum.RECORD, "recordDateGreaterThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record lteq datetime date. */
  RECORD_LTEQ_DATETIME_DATE(EntityTypeEnum.RECORD, "recordDateLessThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record eq datetime date record. */
  RECORD_EQ_DATETIME_DATE_RECORD(EntityTypeEnum.RECORD, "recordDateEqualsRecord", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.DATE),

  /** The record dist datetime date record. */
  RECORD_DIST_DATETIME_DATE_RECORD(EntityTypeEnum.RECORD, "recordDateDistinctRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record gt datetime date record. */
  RECORD_GT_DATETIME_DATE_RECORD(EntityTypeEnum.RECORD, "recordDateGreaterThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record lt datetime date record. */
  RECORD_LT_DATETIME_DATE_RECORD(EntityTypeEnum.RECORD, "recordDateLessThanRecord",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The record gteq datetime date record. */
  RECORD_GTEQ_DATETIME_DATE_RECORD(EntityTypeEnum.RECORD, "recordDateGreaterThanOrEqualsThanRecord",
      JavaType.TIMESTAMP, JavaType.DATE, JavaType.DATE),

  /** The record lteq datetime date record. */
  RECORD_LTEQ_DATETIME_DATE_RECORD(EntityTypeEnum.RECORD, "recordDateLessThanOrEqualsThanRecord",
      JavaType.TIMESTAMP, JavaType.DATE, JavaType.DATE),

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

  /** The field null. */
  FIELD_NULL(EntityTypeEnum.FIELD, "fieldNull", JavaType.BOOLEAN, JavaType.OBJECT),

  /** The field not null. */
  FIELD_NOT_NULL(EntityTypeEnum.FIELD, "fieldNotNull", JavaType.BOOLEAN, JavaType.OBJECT),

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

  /** The field eq day datetime. */
  FIELD_EQ_DAY_DATETIME(EntityTypeEnum.FIELD, "fieldDaytimeEquals", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The field dist day datetime. */
  FIELD_DIST_DAY_DATETIME(EntityTypeEnum.FIELD, "fieldDaytimeDistinct", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The field gt day datetime. */
  FIELD_GT_DAY_DATETIME(EntityTypeEnum.FIELD, "fieldDaytimeGreaterThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The field lt day datetime. */
  FIELD_LT_DAY_DATETIME(EntityTypeEnum.FIELD, "fieldDaytimeLessThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The field gteq day datetime. */
  FIELD_GTEQ_DAY_DATETIME(EntityTypeEnum.FIELD, "fieldDaytimeGreaterThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The field lteq day datetime. */
  FIELD_LTEQ_DAY_DATETIME(EntityTypeEnum.FIELD, "fieldDaytimeLessThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

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

  /** The field eq month datetime. */
  FIELD_EQ_MONTH_DATETIME(EntityTypeEnum.FIELD, "fieldMonthtimeEquals", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The field dist month datetime. */
  FIELD_DIST_MONTH_DATETIME(EntityTypeEnum.FIELD, "fieldMonthtimeDistinct", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The field gt month datetime. */
  FIELD_GT_MONTH_DATETIME(EntityTypeEnum.FIELD, "fieldMonthtimeGreaterThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The field lt month datetime. */
  FIELD_LT_MONTH_DATETIME(EntityTypeEnum.FIELD, "fieldMonthtimeLessThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The field gteq month datetime. */
  FIELD_GTEQ_MONTH_DATETIME(EntityTypeEnum.FIELD, "fieldMonthtimeGreaterThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The field lteq month datetime. */
  FIELD_LTEQ_MONTH_DATETIME(EntityTypeEnum.FIELD, "fieldMonthtimeLessThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

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

  /** The field eq year datetime. */
  FIELD_EQ_YEAR_DATETIME(EntityTypeEnum.FIELD, "fieldYeartimeEquals", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The field dist year datetime. */
  FIELD_DIST_YEAR_DATETIME(EntityTypeEnum.FIELD, "fieldYeartimeDistinct", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The field gt year datetime. */
  FIELD_GT_YEAR_DATETIME(EntityTypeEnum.FIELD, "fieldYeartimeGreaterThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The field lt year datetime. */
  FIELD_LT_YEAR_DATETIME(EntityTypeEnum.FIELD, "fieldYeartimeLessThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The field gteq year datetime. */
  FIELD_GTEQ_YEAR_DATETIME(EntityTypeEnum.FIELD, "fieldYeartimeGreaterThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

  /** The field lteq year datetime. */
  FIELD_LTEQ_YEAR_DATETIME(EntityTypeEnum.FIELD, "fieldYeartimeLessThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.NUMBER),

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
      JavaType.DATE, JavaType.DATE),

  // Datetime operators

  /** The field eq datetime. */
  FIELD_EQ_DATETIME(EntityTypeEnum.FIELD, "fieldDatetimeEquals", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The field dist datetime. */
  FIELD_DIST_DATETIME(EntityTypeEnum.FIELD, "fieldDatetimeDistinct", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The field gt datetime. */
  FIELD_GT_DATETIME(EntityTypeEnum.FIELD, "fieldDatetimeGreaterThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The field lt datetime. */
  FIELD_LT_DATETIME(EntityTypeEnum.FIELD, "fieldDatetimeLessThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The field gteq datetime. */
  FIELD_GTEQ_DATETIME(EntityTypeEnum.FIELD, "fieldDatetimeGreaterThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  /** The field lteq datetime. */
  FIELD_LTEQ_DATETIME(EntityTypeEnum.FIELD, "fieldDatetimeLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.TIMESTAMP),

  // Date to datetime operators

  /** The field eq date datetime. */
  FIELD_EQ_DATE_DATETIME(EntityTypeEnum.FIELD, "fieldDateEquals", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.TIMESTAMP),

  /** The field dist date datetime. */
  FIELD_DIST_DATE_DATETIME(EntityTypeEnum.FIELD, "fieldDateDistinct", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.TIMESTAMP),

  /** The field gt date datetime. */
  FIELD_GT_DATE_DATETIME(EntityTypeEnum.FIELD, "fieldDateGreaterThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.TIMESTAMP),

  /** The field lt date datetime. */
  FIELD_LT_DATE_DATETIME(EntityTypeEnum.FIELD, "fieldDateLessThan", JavaType.BOOLEAN, JavaType.DATE,
      JavaType.TIMESTAMP),

  /** The field gteq date datetime. */
  FIELD_GTEQ_DATE_DATETIME(EntityTypeEnum.FIELD, "fieldDateGreaterThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.DATE, JavaType.TIMESTAMP),

  /** The field lteq date datetime. */
  FIELD_LTEQ_DATE_DATETIME(EntityTypeEnum.FIELD, "fieldDateLessThanOrEqualsThan", JavaType.BOOLEAN,
      JavaType.DATE, JavaType.TIMESTAMP),

  // Datetime to date operators

  /** The field eq datetime date. */
  FIELD_EQ_DATETIME_DATE(EntityTypeEnum.FIELD, "fieldDatetimeEquals", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.DATE),

  /** The field dist datetime date. */
  FIELD_DIST_DATETIME_DATE(EntityTypeEnum.FIELD, "fieldDatetimeDistinct", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.DATE),

  /** The field gt datetime date. */
  FIELD_GT_DATETIME_DATE(EntityTypeEnum.FIELD, "fieldDatetimeGreaterThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.DATE),

  /** The field lt datetime date. */
  FIELD_LT_DATETIME_DATE(EntityTypeEnum.FIELD, "fieldDatetimeLessThan", JavaType.BOOLEAN,
      JavaType.TIMESTAMP, JavaType.DATE),

  /** The field gteq datetime date. */
  FIELD_GTEQ_DATETIME_DATE(EntityTypeEnum.FIELD, "fieldDatetimeGreaterThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE),

  /** The field lteq datetime date. */
  FIELD_LTEQ_DATETIME_DATE(EntityTypeEnum.FIELD, "fieldDatetimeLessThanOrEqualsThan",
      JavaType.BOOLEAN, JavaType.TIMESTAMP, JavaType.DATE);

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
      map.putIfAbsent(e.getFunctionName(), e);
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
