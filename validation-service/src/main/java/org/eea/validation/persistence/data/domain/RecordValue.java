package org.eea.validation.persistence.data.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** The Class RecordValue. */
@Entity
@Getter
@Setter
@ToString
@Table(name = "RECORD_VALUE")
public class RecordValue {

  /** The id. */
  @Id
  @SequenceGenerator(name = "record_sequence_generator", sequenceName = "record_sequence",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "record_sequence_generator")
  @Column(name = "ID", columnDefinition = "serial")
  private String id;

  /** The id record schema. */
  @Column(name = "ID_RECORD_SCHEMA")
  private String idRecordSchema;

  /** The dataset partition id. */
  @Column(name = "DATASET_PARTITION_ID")
  private Long datasetPartitionId;

  /** The data provider code. */
  @Column(name = "DATA_PROVIDER_CODE")
  private String dataProviderCode;

  /** The table value. */
  @ManyToOne
  @JoinColumn(name = "ID_TABLE")
  private TableValue tableValue;

  /** The fields. */
  @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<FieldValue> fields;

  /** The record validations. */
  @OneToMany(mappedBy = "recordValue",
      cascade = {CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH}, orphanRemoval = false)
  private List<RecordValidation> recordValidations;

  /** The sort criteria. */
  @Transient
  private String sortCriteria;

  /** The level error. */
  @Transient
  private ErrorTypeEnum levelError;

  /** The fields map. */
  @Transient
  private Map<String, String> fieldsMap;

  /** The Constant DATE_FORMAT. */
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(datasetPartitionId, fields, id, idRecordSchema, tableValue);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final RecordValue other = (RecordValue) obj;
    return Objects.equals(datasetPartitionId, other.datasetPartitionId)
        && Objects.equals(fields, other.fields) && Objects.equals(id, other.id)
        && Objects.equals(idRecordSchema, other.idRecordSchema)
        && Objects.equals(tableValue, other.tableValue);
  }

  /**
   * Initialize fields map.
   */
  public void initializeFieldsMap() {
    fieldsMap = new HashMap<>();
    if (null != fields && !fields.isEmpty()) {
      for (FieldValue field : fields) {
        fieldsMap.put(field.getIdFieldSchema(), field.getValue());
      }
    }
  }

  /**
   * Record if then.
   *
   * @param argIf the arg if
   * @param argThen the arg then
   * @return true, if successful
   */
  public boolean recordIfThen(boolean argIf, boolean argThen) {
    return !argIf || argThen;
  }

  /**
   * Record and.
   *
   * @param condition1 the condition 1
   * @param condition2 the condition 2
   * @return true, if successful
   */
  public boolean recordAnd(boolean condition1, boolean condition2) {
    return condition1 && condition2;
  }

  /**
   * Record or.
   *
   * @param condition1 the condition 1
   * @param condition2 the condition 2
   * @return true, if successful
   */
  public boolean recordOr(boolean condition1, boolean condition2) {
    return condition1 || condition2;
  }

  /**
   * Record not.
   *
   * @param condition the condition
   * @return true, if successful
   */
  public boolean recordNot(boolean condition) {
    return !condition;
  }

  /**
   * Record number equals.
   *
   * @param fieldSchemaId the field schema id
   * @param number the number
   * @return true, if successful
   */
  public boolean recordNumberEquals(String fieldSchemaId, Number number) {
    try {
      return Double.valueOf(fieldsMap.get(fieldSchemaId)).equals(number.doubleValue());
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
  public boolean recordNumberDistinct(String fieldSchemaId, Number number) {
    try {
      return !Double.valueOf(fieldsMap.get(fieldSchemaId)).equals(number.doubleValue());
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
  public boolean recordNumberGreaterThan(String fieldSchemaId, Number number) {
    try {
      return Double.valueOf(fieldsMap.get(fieldSchemaId)) > number.doubleValue();
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
  public boolean recordNumberLessThan(String fieldSchemaId, Number number) {
    try {
      return Double.valueOf(fieldsMap.get(fieldSchemaId)) < number.doubleValue();
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
  public boolean recordNumberGreaterThanOrEqualsThan(String fieldSchemaId, Number number) {
    try {
      return Double.valueOf(fieldsMap.get(fieldSchemaId)) >= number.doubleValue();
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
  public boolean recordNumberLessThanOrEqualsThan(String fieldSchemaId, Number number) {
    try {
      return Double.valueOf(fieldsMap.get(fieldSchemaId)) <= number.doubleValue();
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
  public boolean recordNumberEqualsRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      return Double.valueOf(fieldsMap.get(fieldSchemaId1))
          .equals(Double.valueOf(fieldsMap.get(fieldSchemaId2)));
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
  public boolean recordNumberDistinctRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      return !Double.valueOf(fieldsMap.get(fieldSchemaId1))
          .equals(Double.valueOf(fieldsMap.get(fieldSchemaId2)));
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
  public boolean recordNumberGreaterThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      return Double.valueOf(fieldsMap.get(fieldSchemaId1)) > Double
          .valueOf(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordNumberLessThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      return Double.valueOf(fieldsMap.get(fieldSchemaId1)) < Double
          .valueOf(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordNumberGreaterThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      return Double.valueOf(fieldsMap.get(fieldSchemaId1)) >= Double
          .valueOf(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordNumberLessThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      return Double.valueOf(fieldsMap.get(fieldSchemaId1)) <= Double
          .valueOf(fieldsMap.get(fieldSchemaId2));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record number matches.
   *
   * @param fieldSchemaId the field schema id
   * @param dataMach the data mach
   * @return true, if successful
   */
  public boolean recordNumberMatches(String fieldSchemaId, String dataMach) {
    boolean validateReturn;
    try {
      validateReturn =
          -1 == fieldsMap.get(fieldSchemaId).indexOf(fieldsMap.get(dataMach)) ? false : true;
    } catch (Exception e) {
      validateReturn = true;
    }
    return validateReturn;
  }

  /**
   * Record string length.
   *
   * @param fieldSchemaId the field schema id
   * @return the string
   */
  public String recordStringLength(String fieldSchemaId) {
    try {
      return "" + fieldsMap.get(fieldSchemaId).length();
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
  public boolean recordStringLengthEquals(String fieldSchemaId, Number number) {
    try {
      return fieldsMap.get(fieldSchemaId).length() == number.intValue();
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * Record string length equals.
   *
   * @param fieldSchemaId the field schema id
   * @param number the number
   * @return true, if successful
   */
  public boolean recordStringLengthEquals(String fieldSchemaId, String number) {
    try {
      return fieldsMap.get(fieldSchemaId).length() != Integer.valueOf(number);
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
  public boolean recordStringLengthDistinct(String fieldSchemaId, Number number) {
    try {
      return fieldsMap.get(fieldSchemaId).length() != number.intValue();
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
  public boolean recordStringLengthDistinct(String fieldSchemaId, String number) {
    try {
      return fieldsMap.get(fieldSchemaId).length() != Integer.valueOf(number);
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
  public boolean recordStringLengthGreaterThan(String fieldSchemaId, Number number) {
    try {
      return fieldsMap.get(fieldSchemaId).length() > number.intValue();
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
  public boolean recordStringLengthGreaterThan(String fieldSchemaId, String number) {
    try {
      return fieldsMap.get(fieldSchemaId).length() > Integer.valueOf(number);
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
  public boolean recordStringLengthLessThan(String fieldSchemaId, Number number) {
    try {
      return fieldsMap.get(fieldSchemaId).length() < number.intValue();
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
  public boolean recordStringLengthLessThan(String fieldSchemaId, String number) {
    try {
      return fieldsMap.get(fieldSchemaId).length() < Integer.valueOf(number);
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
  public boolean recordStringLengthGreaterThanOrEqualsThan(String fieldSchemaId, Number number) {
    try {
      return fieldsMap.get(fieldSchemaId).length() >= number.intValue();
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
  public boolean recordStringLengthGreaterThanOrEqualsThan(String fieldSchemaId, String number) {
    try {
      return fieldsMap.get(fieldSchemaId).length() >= Integer.valueOf(number);
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
  public boolean recordStringLengthLessThanOrEqualsThan(String fieldSchemaId, Number number) {
    try {
      return fieldsMap.get(fieldSchemaId).length() <= number.intValue();
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
  public boolean recordStringLengthLessThanOrEqualsThan(String fieldSchemaId, String number) {
    try {
      return fieldsMap.get(fieldSchemaId).length() <= Integer.valueOf(number);
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
  public boolean recordStringEquals(String fieldSchemaId, String otherString) {
    try {
      return fieldsMap.get(fieldSchemaId).equals(otherString);
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
  public boolean recordStringEqualsIgnoreCase(String fieldSchemaId, String otherString) {
    try {
      return fieldsMap.get(fieldSchemaId).equalsIgnoreCase(otherString);
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
  public boolean recordStringMatches(String fieldSchemaId, String regex) {
    boolean validateReturn;
    try {
      validateReturn =
          -1 == fieldsMap.get(fieldSchemaId).indexOf(fieldsMap.get(regex)) ? false : true;
    } catch (Exception e) {
      validateReturn = true;
    }
    return validateReturn;
  }

  /**
   * Record string equals record.
   *
   * @param fieldSchemaId1 the field schema id 1
   * @param fieldSchemaId2 the field schema id 2
   * @return true, if successful
   */
  public boolean recordStringEqualsRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      return fieldsMap.get(fieldSchemaId1).equals(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordStringEqualsIgnoreCaseRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      return fieldsMap.get(fieldSchemaId1).equalsIgnoreCase(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordStringMatchesRecord(String fieldSchemaId1, String fieldSchemaId2) {
    boolean validateReturn;
    try {
      validateReturn =
          -1 == fieldsMap.get(fieldSchemaId2).indexOf(fieldsMap.get(fieldSchemaId1)) ? false : true;
    } catch (Exception e) {
      validateReturn = true;
    }
    return validateReturn;
  }

  /**
   * Record day equals.
   *
   * @param fieldSchemaId the field schema id
   * @param day the day
   * @return true, if successful
   */
  public boolean recordDayEquals(String fieldSchemaId, Number day) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordDayDistinct(String fieldSchemaId, Number day) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordDayGreaterThan(String fieldSchemaId, Number day) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordDayLessThan(String fieldSchemaId, Number day) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordDayGreaterThanOrEqualsThan(String fieldSchemaId, Number day) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordDayLessThanOrEqualsThan(String fieldSchemaId, Number day) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordDayEqualsRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordDayDistinctRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordDayGreaterThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordDayLessThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordDayGreaterThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordDayLessThanOrEqualsThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordDayEqualsRecordNumber(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordDayDistinctRecordNumber(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordDayGreaterThanRecordNumber(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordDayLessThanRecordNumber(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordDayGreaterThanOrEqualsThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordDayLessThanOrEqualsThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordMonthEquals(String fieldSchemaId, Number month) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordMonthDistinct(String fieldSchemaId, Number month) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordMonthGreaterThan(String fieldSchemaId, Number month) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordMonthLessThan(String fieldSchemaId, Number month) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordMonthGreaterThanOrEqualsThan(String fieldSchemaId, Number month) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordMonthLessThanOrEqualsThan(String fieldSchemaId, Number month) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordMonthEqualsRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordMonthDistinctRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordMonthGreaterThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordMonthLessThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordMonthGreaterThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordMonthLessThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordMonthEqualsRecordNumber(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordMonthDistinctRecordNumber(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordMonthGreaterThanRecordNumber(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordMonthLessThanRecordNumber(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordMonthGreaterThanOrEqualsThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordMonthLessThanOrEqualsThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordYearEquals(String fieldSchemaId, Number year) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordYearDistinct(String fieldSchemaId, Number year) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordYearGreaterThan(String fieldSchemaId, Number year) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordYearLessThan(String fieldSchemaId, Number year) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordYearGreaterThanOrEqualsThan(String fieldSchemaId, Number year) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordYearLessThanOrEqualsThan(String fieldSchemaId, Number year) {
    try {
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordYearEqualsRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordYearDistinctRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordYearGreaterThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordYearLessThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordYearGreaterThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordYearLessThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordYearEqualsRecordNumber(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordYearDistinctRecordNumber(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordYearGreaterThanRecordNumber(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordYearLessThanRecordNumber(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordYearGreaterThanOrEqualsThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordYearLessThanOrEqualsThanRecordNumber(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      int number = Integer.parseInt(fieldsMap.get(fieldSchemaId2));
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
  public boolean recordDateEquals(String fieldSchemaId, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordDateDistinct(String fieldSchemaId, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordDateGreaterThan(String fieldSchemaId, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordDateLessThan(String fieldSchemaId, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordDateLessThanOrEqualsThan(String fieldSchemaId, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordDateGreaterThanOrEqualsThan(String fieldSchemaId, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(fieldsMap.get(fieldSchemaId), DATE_FORMAT);
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
  public boolean recordDateEqualsRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordDateDistinctRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordDateGreaterThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordDateLessThanRecord(String fieldSchemaId1, String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordDateGreaterThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
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
  public boolean recordDateLessThanOrEqualsThanRecord(String fieldSchemaId1,
      String fieldSchemaId2) {
    try {
      LocalDate date1 = LocalDate.parse(fieldsMap.get(fieldSchemaId1), DATE_FORMAT);
      LocalDate date2 = LocalDate.parse(fieldsMap.get(fieldSchemaId2), DATE_FORMAT);
      return date1.isAfter(date2) || date1.equals(date2);
    } catch (Exception e) {
      return true;
    }
  }
}
