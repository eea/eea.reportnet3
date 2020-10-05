package org.eea.validation.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValue;

/** The Class RuleOperators. */
public class RuleOperators {

  /** The fields. */
  private static List<FieldValue> fields;

  /** The country code. */
  private static String countryCode;

  /** The Constant DATE_FORMAT. */
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * Instantiates a new rule operators.
   */
  private RuleOperators() {}

  /**
   * Sets the entity for RecordValue.
   *
   * @param recordValue the record value
   * @return true, if successful
   */
  public static boolean setEntity(RecordValue recordValue) {
    fields = recordValue.getFields();
    countryCode = recordValue.getDataProviderCode();
    if (null == countryCode) {
      countryCode = "XX";
    }
    return true;
  }

  /**
   * Sets the entity for FieldValue.
   *
   * @param fieldValue the field value
   * @return true, if successful
   */
  public static boolean setEntity(FieldValue fieldValue) {
    countryCode = fieldValue.getRecord().getDataProviderCode();
    if (null == countryCode) {
      countryCode = "XX";
    }
    return true;
  }

  /**
   * Do nothing when its called with an entity different of RecordValue.
   *
   * @param otherEntity the other entity
   * @return true, if successfule
   */
  public static boolean setEntity(Object otherEntity) {
    return true;
  }

  /**
   * Gets the value.
   *
   * @param fieldSchemaId the field schema id
   * @return the value
   */
  private static String getValue(String fieldSchemaId) {
    for (FieldValue field : fields) {
      if (field.getIdFieldSchema().equals(fieldSchemaId)) {
        return field.getValue();
      }
    }
    return "";
  }

  /**
   * Replace keywords.
   *
   * @param regex the regex
   * @return the string
   */
  private static String replaceKeywords(String regex) {
    if (regex.contains("{%R3_COUNTRY_CODE%}")) {
      regex = regex.replace("{%R3_COUNTRY_CODE%}", countryCode);
    }
    return regex;
  }

  /**
   * Record if then.
   *
   * @param argIf the arg if
   * @param argThen the arg then
   * @return true, if successful
   */
  public static boolean recordIfThen(boolean argIf, boolean argThen) {
    if (argIf) {
      return argThen;
    } else {
      return true;
    }
  }

  /**
   * Record and.
   *
   * @param condition1 the condition 1
   * @param condition2 the condition 2
   * @return true, if successful
   */
  public static boolean recordAnd(boolean condition1, boolean condition2) {
    return condition1 && condition2;
  }

  /**
   * Record or.
   *
   * @param condition1 the condition 1
   * @param condition2 the condition 2
   * @return true, if successful
   */
  public static boolean recordOr(boolean condition1, boolean condition2) {
    return condition1 || condition2;
  }

  /**
   * Record not.
   *
   * @param condition the condition
   * @return true, if successful
   */
  public static boolean recordNot(boolean condition) {
    return !condition;
  }

  /**
   * Record null.
   *
   * @param value the value
   * @return true, if successful
   */
  public static boolean recordNull(Object value) {
    try {
      return null != value && !((String) value).isEmpty();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record number equals.
   *
   * @param fieldSchemaId the field schema id
   * @param number the number
   * @return true, if successful
   */
  public static boolean recordNumberEquals(String fieldSchemaId, Number number) {
    try {
      return Double.valueOf(getValue(fieldSchemaId)).equals(number.doubleValue());
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record number distinct.
   *
   * @param fieldSchemaId the field schema id
   * @param number the number
   * @return true, if successful
   */
  public static boolean recordNumberDistinct(String fieldSchemaId, Number number) {
    try {
      return !Double.valueOf(getValue(fieldSchemaId)).equals(number.doubleValue());
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record number greater than.
   *
   * @param fieldSchemaId the field schema id
   * @param number the number
   * @return true, if successful
   */
  public static boolean recordNumberGreaterThan(String fieldSchemaId, Number number) {
    try {
      return Double.valueOf(getValue(fieldSchemaId)) > number.doubleValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record number less than.
   *
   * @param fieldSchemaId the field schema id
   * @param number the number
   * @return true, if successful
   */
  public static boolean recordNumberLessThan(String fieldSchemaId, Number number) {
    try {
      return Double.valueOf(getValue(fieldSchemaId)) < number.doubleValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record number greater than or equals than.
   *
   * @param fieldSchemaId the field schema id
   * @param number the number
   * @return true, if successful
   */
  public static boolean recordNumberGreaterThanOrEqualsThan(String fieldSchemaId, Number number) {
    try {
      return Double.valueOf(getValue(fieldSchemaId)) >= number.doubleValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record number less than or equals than.
   *
   * @param fieldSchemaId the field schema id
   * @param number the number
   * @return true, if successful
   */
  public static boolean recordNumberLessThanOrEqualsThan(String fieldSchemaId, Number number) {
    try {
      return Double.valueOf(getValue(fieldSchemaId)) <= number.doubleValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record number equals record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordNumberEqualsRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      return recordNumberEquals(fieldSchemaId1, Double.valueOf(getValue(fieldSchemaId2)));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record number distinct record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordNumberDistinctRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      return recordNumberDistinct(fieldSchemaId1, Double.valueOf(getValue(fieldSchemaId2)));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record number greater than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordNumberGreaterThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      return recordNumberGreaterThan(fieldSchemaId1, Double.valueOf(getValue(fieldSchemaId2)));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record number less than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordNumberLessThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      return recordNumberLessThan(fieldSchemaId1, Double.valueOf(getValue(fieldSchemaId2)));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record number greater than or equals than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordNumberGreaterThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      return recordNumberGreaterThanOrEqualsThan(fieldSchemaId1,
          Double.valueOf(getValue(fieldSchemaId2)));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record number less than or equals than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordNumberLessThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      return recordNumberLessThanOrEqualsThan(fieldSchemaId1,
          Double.valueOf(getValue(fieldSchemaId2)));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record number matches.
   *
   * @param fieldSchemaId the field schema id
   * @param regex the regex
   * @return true, if successful
   */
  public static boolean recordNumberMatches(String fieldSchemaId, String regex) {
    try {
      return getValue(fieldSchemaId).matches(regex);
    } catch (PatternSyntaxException e) {
      return false;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string length.
   *
   * @param fieldSchemaId the field schema id
   * @return the string
   */
  public static Integer recordStringLength(String fieldSchemaId) {
    try {
      return Integer.valueOf(getValue(fieldSchemaId).length());
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Record string length equals.
   *
   * @param fieldSchemaId the field schema id
   * @param number the number
   * @return true, if successful
   */
  public static boolean recordStringLengthEquals(String fieldSchemaId, Number number) {
    try {
      return getValue(fieldSchemaId).length() == number.intValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string length distinct.
   *
   * @param fieldSchemaId the field schema id
   * @param number the number
   * @return true, if successful
   */
  public static boolean recordStringLengthDistinct(String fieldSchemaId, Number number) {
    try {
      return getValue(fieldSchemaId).length() != number.intValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string length greater than.
   *
   * @param fieldSchemaId the field schema id
   * @param number the number
   * @return true, if successful
   */
  public static boolean recordStringLengthGreaterThan(String fieldSchemaId, Number number) {
    try {
      return getValue(fieldSchemaId).length() > number.intValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string length less than.
   *
   * @param fieldSchemaId the field schema id
   * @param number the number
   * @return true, if successful
   */
  public static boolean recordStringLengthLessThan(String fieldSchemaId, Number number) {
    try {
      return getValue(fieldSchemaId).length() < number.intValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string length greater than or equals than.
   *
   * @param fieldSchemaId the field schema id
   * @param number the number
   * @return true, if successful
   */
  public static boolean recordStringLengthGreaterThanOrEqualsThan(String fieldSchemaId,
      Number number) {
    try {
      return getValue(fieldSchemaId).length() >= number.intValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string length less than or equals than.
   *
   * @param fieldSchemaId the field schema id
   * @param number the number
   * @return true, if successful
   */
  public static boolean recordStringLengthLessThanOrEqualsThan(String fieldSchemaId,
      Number number) {
    try {
      return getValue(fieldSchemaId).length() <= number.intValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string length equals record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordStringLengthEqualsRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      return recordStringLengthEquals(fieldSchemaId1, Integer.valueOf(getValue(fieldSchemaId2)));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string length distinct record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordStringLengthDistinctRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      return recordStringLengthDistinct(fieldSchemaId1, Integer.valueOf(getValue(fieldSchemaId2)));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string length greater than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordStringLengthGreaterThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      return recordStringLengthGreaterThan(fieldSchemaId1,
          Integer.valueOf(getValue(fieldSchemaId2)));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string length less than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordStringLengthLessThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      return recordStringLengthLessThan(fieldSchemaId1, Integer.valueOf(getValue(fieldSchemaId2)));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string length greater than or equals than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordStringLengthGreaterThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      return recordStringLengthGreaterThanOrEqualsThan(fieldSchemaId1,
          Integer.valueOf(getValue(fieldSchemaId2)));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string length less than or equals than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordStringLengthLessThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      return recordStringLengthLessThanOrEqualsThan(fieldSchemaId1,
          Integer.valueOf(getValue(fieldSchemaId2)));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string equals.
   *
   * @param fieldSchemaId the field schema id
   * @param otherString the other string
   * @return true, if successful
   */
  public static boolean recordStringEquals(String fieldSchemaId, String otherString) {
    try {
      return getValue(fieldSchemaId).equals(otherString);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string equals ignore case.
   *
   * @param fieldSchemaId the field schema id
   * @param otherString the other string
   * @return true, if successful
   */
  public static boolean recordStringEqualsIgnoreCase(String fieldSchemaId, String otherString) {
    try {
      return getValue(fieldSchemaId).equalsIgnoreCase(otherString);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string matches.
   *
   * @param fieldSchemaId the field schema id
   * @param regex the regex
   * @return true, if successful
   */
  public static boolean recordStringMatches(String fieldSchemaId, String regex) {
    try {
      return getValue(fieldSchemaId).matches(replaceKeywords(regex));
    } catch (PatternSyntaxException e) {
      return false;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string equals record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordStringEqualsRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      return recordStringEquals(fieldSchemaId1, getValue(fieldSchemaId2));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string equals ignore case record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordStringEqualsIgnoreCaseRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      return recordStringEqualsIgnoreCase(fieldSchemaId1, getValue(fieldSchemaId2));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string matches record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordStringMatchesRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      return recordStringMatches(fieldSchemaId1, getValue(fieldSchemaId2));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day equals.
   *
   * @param fieldSchemaId the field schema id
   * @param day the day
   * @return true, if successful
   */
  public static boolean recordDayEquals(String fieldSchemaId, Number day) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getDayOfMonth() == day.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day distinct.
   *
   * @param fieldSchemaId the field schema id
   * @param day the day
   * @return true, if successful
   */
  public static boolean recordDayDistinct(String fieldSchemaId, Number day) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getDayOfMonth() != day.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day greater than.
   *
   * @param fieldSchemaId the field schema id
   * @param day the day
   * @return true, if successful
   */
  public static boolean recordDayGreaterThan(String fieldSchemaId, Number day) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getDayOfMonth() > day.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day less than.
   *
   * @param fieldSchemaId the field schema id
   * @param day the day
   * @return true, if successful
   */
  public static boolean recordDayLessThan(String fieldSchemaId, Number day) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getDayOfMonth() < day.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day greater than or equals than.
   *
   * @param fieldSchemaId the field schema id
   * @param day the day
   * @return true, if successful
   */
  public static boolean recordDayGreaterThanOrEqualsThan(String fieldSchemaId, Number day) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getDayOfMonth() >= day.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day less than or equals than.
   *
   * @param fieldSchemaId the field schema id
   * @param day the day
   * @return true, if successful
   */
  public static boolean recordDayLessThanOrEqualsThan(String fieldSchemaId, Number day) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getDayOfMonth() <= day.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day equals record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDayEqualsRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getDayOfMonth() == date2.getDayOfMonth();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day distinct record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDayDistinctRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getDayOfMonth() != date2.getDayOfMonth();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day greater than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDayGreaterThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getDayOfMonth() > date2.getDayOfMonth();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day less than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDayLessThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getDayOfMonth() < date2.getDayOfMonth();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day greater than or equals than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDayGreaterThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getDayOfMonth() >= date2.getDayOfMonth();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day less than or equals than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDayLessThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getDayOfMonth() <= date2.getDayOfMonth();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day equals record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDayEqualsRecordNumber(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getDayOfMonth() == number;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day distinct record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDayDistinctRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getDayOfMonth() != number;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day greater than record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDayGreaterThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getDayOfMonth() > number;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day less than record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDayLessThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getDayOfMonth() < number;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day greater than or equals than record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDayGreaterThanOrEqualsThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getDayOfMonth() >= number;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record day less than or equals than record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDayLessThanOrEqualsThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getDayOfMonth() <= number;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record month equals.
   *
   * @param fieldSchemaId the field schema id
   * @param month the month
   * @return true, if successful
   */
  public static boolean recordMonthEquals(String fieldSchemaId, Number month) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getMonthValue() == month.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record month distinct.
   *
   * @param fieldSchemaId the field schema id
   * @param month the month
   * @return true, if successful
   */
  public static boolean recordMonthDistinct(String fieldSchemaId, Number month) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getMonthValue() != month.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record month greater than.
   *
   * @param fieldSchemaId the field schema id
   * @param month the month
   * @return true, if successful
   */
  public static boolean recordMonthGreaterThan(String fieldSchemaId, Number month) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getMonthValue() > month.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record month less than.
   *
   * @param fieldSchemaId the field schema id
   * @param month the month
   * @return true, if successful
   */
  public static boolean recordMonthLessThan(String fieldSchemaId, Number month) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getMonthValue() < month.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record month greater than or equals than.
   *
   * @param fieldSchemaId the field schema id
   * @param month the month
   * @return true, if successful
   */
  public static boolean recordMonthGreaterThanOrEqualsThan(String fieldSchemaId, Number month) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getMonthValue() >= month.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record month less than or equals than.
   *
   * @param fieldSchemaId the field schema id
   * @param month the month
   * @return true, if successful
   */
  public static boolean recordMonthLessThanOrEqualsThan(String fieldSchemaId, Number month) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getMonthValue() <= month.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record month equals record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordMonthEqualsRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getMonthValue() == date2.getMonthValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record month distinct record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordMonthDistinctRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getMonthValue() != date2.getMonthValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record month greater than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordMonthGreaterThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getMonthValue() > date2.getMonthValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record month less than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordMonthLessThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getMonthValue() < date2.getMonthValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record month greater than or equals than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordMonthGreaterThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getMonthValue() >= date2.getMonthValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record month less than or equals than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordMonthLessThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getMonthValue() <= date2.getMonthValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record month equals record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordMonthEqualsRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getMonthValue() == number;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record month distinct record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordMonthDistinctRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getMonthValue() != number;
    } catch (Exception e) {
      return true;
    }
  }


  /**
   * Record month greater than record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordMonthGreaterThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getMonthValue() > number;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record month less than record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordMonthLessThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getMonthValue() < number;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record month greater than or equals than record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordMonthGreaterThanOrEqualsThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getMonthValue() >= number;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record month less than or equals than record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordMonthLessThanOrEqualsThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getMonthValue() <= number;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year equals.
   *
   * @param fieldSchemaId the field schema id
   * @param year the year
   * @return true, if successful
   */
  public static boolean recordYearEquals(String fieldSchemaId, Number year) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getYear() == year.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year distinct.
   *
   * @param fieldSchemaId the field schema id
   * @param year the year
   * @return true, if successful
   */
  public static boolean recordYearDistinct(String fieldSchemaId, Number year) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getYear() != year.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year greater than.
   *
   * @param fieldSchemaId the field schema id
   * @param year the year
   * @return true, if successful
   */
  public static boolean recordYearGreaterThan(String fieldSchemaId, Number year) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getYear() > year.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year less than.
   *
   * @param fieldSchemaId the field schema id
   * @param year the year
   * @return true, if successful
   */
  public static boolean recordYearLessThan(String fieldSchemaId, Number year) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getYear() < year.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year greater than or equals than.
   *
   * @param fieldSchemaId the field schema id
   * @param year the year
   * @return true, if successful
   */
  public static boolean recordYearGreaterThanOrEqualsThan(String fieldSchemaId, Number year) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getYear() >= year.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year less than or equals than.
   *
   * @param fieldSchemaId the field schema id
   * @param year the year
   * @return true, if successful
   */
  public static boolean recordYearLessThanOrEqualsThan(String fieldSchemaId, Number year) {
    try {
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.getYear() <= year.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year equals record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordYearEqualsRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getYear() == date2.getYear();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year distinct record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordYearDistinctRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getYear() != date2.getYear();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year greater than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordYearGreaterThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getYear() > date2.getYear();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year less than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordYearLessThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getYear() < date2.getYear();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year greater than or equals than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordYearGreaterThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getYear() >= date2.getYear();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year less than or equals than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordYearLessThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.getYear() <= date2.getYear();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year equals record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordYearEqualsRecordNumber(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getYear() == number;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year distinct record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordYearDistinctRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getYear() != number;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year greater than record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordYearGreaterThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getYear() > number;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year less than record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordYearLessThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getYear() < number;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year greater than or equals than record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordYearGreaterThanOrEqualsThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getYear() >= number;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record year less than or equals than record number.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordYearLessThanOrEqualsThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(getValue(fieldSchemaId2));
      return date.getYear() <= number;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record date equals.
   *
   * @param fieldSchemaId the field schema id
   * @param date the date
   * @return true, if successful
   */
  public static boolean recordDateEquals(String fieldSchemaId, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.equals(ruleDate);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record date distinct.
   *
   * @param fieldSchemaId the field schema id
   * @param date the date
   * @return true, if successful
   */
  public static boolean recordDateDistinct(String fieldSchemaId, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return !fieldDate.equals(ruleDate);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record date greater than.
   *
   * @param fieldSchemaId the field schema id
   * @param date the date
   * @return true, if successful
   */
  public static boolean recordDateGreaterThan(String fieldSchemaId, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.isAfter(ruleDate);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record date less than.
   *
   * @param fieldSchemaId the field schema id
   * @param date the date
   * @return true, if successful
   */
  public static boolean recordDateLessThan(String fieldSchemaId, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.isBefore(ruleDate);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record date less than or equals than.
   *
   * @param fieldSchemaId the field schema id
   * @param date the date
   * @return true, if successful
   */
  public static boolean recordDateLessThanOrEqualsThan(String fieldSchemaId, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.isBefore(ruleDate) || fieldDate.equals(ruleDate);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record date greater than or equals than.
   *
   * @param fieldSchemaId the field schema id
   * @param date the date
   * @return true, if successful
   */
  public static boolean recordDateGreaterThanOrEqualsThan(String fieldSchemaId, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(getValue(fieldSchemaId), DATE_FORMAT);
      return fieldDate.isAfter(ruleDate) || fieldDate.equals(ruleDate);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record date equals record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDateEqualsRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.equals(date2);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record date distinct record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDateDistinctRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return !date1.equals(date2);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record date greater than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDateGreaterThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.isAfter(date2);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record date less than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDateLessThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.isBefore(date2);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record date greater than or equals than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDateGreaterThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.isBefore(date2) || date1.equals(date2);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record date less than or equals than record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public static boolean recordDateLessThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(getValue(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(getValue(fieldSchemaId2), DATE_FORMAT);
      return date1.isAfter(date2) || date1.equals(date2);
    } catch (Exception e) {
      return true;
    }
  }

  // **********************************************************************
  // ******************************* FIELDS *******************************
  // **********************************************************************

  /**
   * Field and.
   *
   * @param arg1 the arg 1
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public static boolean fieldAnd(boolean arg1, boolean arg2) {
    return arg1 && arg2;
  }

  /**
   * Field or.
   *
   * @param arg1 the arg 1
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public static boolean fieldOr(boolean arg1, boolean arg2) {
    return arg1 || arg2;
  }

  /**
   * Field not.
   *
   * @param arg the arg
   * @return true, if successful
   */
  public static boolean fieldNot(boolean arg) {
    return !arg;
  }

  /**
   * Field null.
   *
   * @param value the value
   * @return true, if successful
   */
  public static boolean fieldNull(Object value) {
    try {
      return null != value && !((String) value).isEmpty();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field number equals.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public static boolean fieldNumberEquals(String value, Number arg2) {
    try {
      return Double.valueOf(value).equals(arg2.doubleValue());
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field number distinct.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public static boolean fieldNumberDistinct(String value, Number arg2) {
    try {
      return !Double.valueOf(value).equals(arg2.doubleValue());
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field number greater than.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public static boolean fieldNumberGreaterThan(String value, Number arg2) {
    try {
      return Double.valueOf(value) > arg2.doubleValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field number less than.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public static boolean fieldNumberLessThan(String value, Number arg2) {
    try {
      return Double.valueOf(value) < arg2.doubleValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field number greater than or equals than.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public static boolean fieldNumberGreaterThanOrEqualsThan(String value, Number arg2) {
    try {
      return Double.valueOf(value) >= arg2.doubleValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field number less than or equals than.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public static boolean fieldNumberLessThanOrEqualsThan(String value, Number arg2) {
    try {
      return Double.valueOf(value) <= arg2.doubleValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field number matches.
   *
   * @param value the value
   * @param regex the regex
   * @return true, if successful
   */
  public static boolean fieldNumberMatches(String value, String regex) {
    try {
      return value.matches(regex);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field string length.
   *
   * @param value the value
   * @return the string
   */
  public static String fieldStringLength(String value) {
    try {
      return "" + value.length();
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Field string equals.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public static boolean fieldStringEquals(String value, String arg2) {
    try {
      return value.equals(arg2);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field string equals ignore case.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public static boolean fieldStringEqualsIgnoreCase(String value, String arg2) {
    try {
      return value.equalsIgnoreCase(arg2);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field string matches.
   *
   * @param value the value
   * @param regex the regex
   * @return true, if successful
   */
  public static boolean fieldStringMatches(String value, String regex) {
    try {
      return value.matches(replaceKeywords(regex));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field day equals.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldDayEquals(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getDayOfMonth() == number.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field day distinct.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldDayDistinct(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getDayOfMonth() != number.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field day greater than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldDayGreaterThan(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getDayOfMonth() > number.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field day less than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldDayLessThan(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getDayOfMonth() < number.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field day greater than or equals than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldDayGreaterThanOrEqualsThan(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getDayOfMonth() >= number.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field day less than or equals than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldDayLessThanOrEqualsThan(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getDayOfMonth() <= number.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field month equals.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldMonthEquals(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getMonthValue() == number.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field month distinct.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldMonthDistinct(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getMonthValue() != number.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field month greater than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldMonthGreaterThan(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getMonthValue() > number.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field month less than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldMonthLessThan(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getMonthValue() < number.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field month greater than or equals than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldMonthGreaterThanOrEqualsThan(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getMonthValue() >= number.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field month less than or equals than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldMonthLessThanOrEqualsThan(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getMonthValue() <= number.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field year equals.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldYearEquals(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getYear() == number.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field year distinct.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldYearDistinct(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getYear() != number.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field year greater than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldYearGreaterThan(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getYear() > number.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field year less than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldYearLessThan(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getYear() < number.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field year greater than or equals than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldYearGreaterThanOrEqualsThan(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getYear() >= number.longValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field year less than or equals than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public static boolean fieldYearLessThanOrEqualsThan(String value, Number number) {
    try {
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.getYear() <= number.longValue();
    } catch (Exception e) {
      return true;
    }

  }

  /**
   * Field date equals.
   *
   * @param value the value
   * @param date the date
   * @return true, if successful
   */
  public static boolean fieldDateEquals(String value, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.equals(ruleDate);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field date distinct.
   *
   * @param value the value
   * @param date the date
   * @return true, if successful
   */
  public static boolean fieldDateDistinct(String value, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return !fieldDate.equals(ruleDate);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field date greater than.
   *
   * @param value the value
   * @param date the date
   * @return true, if successful
   */
  public static boolean fieldDateGreaterThan(String value, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.isAfter(ruleDate);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field date less than.
   *
   * @param value the value
   * @param date the date
   * @return true, if successful
   */
  public static boolean fieldDateLessThan(String value, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.isBefore(ruleDate);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field date greater than or equals than.
   *
   * @param value the value
   * @param date the date
   * @return true, if successful
   */
  public static boolean fieldDateGreaterThanOrEqualsThan(String value, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.isAfter(ruleDate) || fieldDate.equals(ruleDate);
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Field date less than or equals than.
   *
   * @param value the value
   * @param date the date
   * @return true, if successful
   */
  public static boolean fieldDateLessThanOrEqualsThan(String value, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.isBefore(ruleDate) || fieldDate.equals(ruleDate);
    } catch (Exception e) {
      return true;
    }
  }
}
