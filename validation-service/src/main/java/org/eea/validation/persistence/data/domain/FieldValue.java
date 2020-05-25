package org.eea.validation.persistence.data.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

/** The Class FieldValue. */
@Entity
@Getter
@Setter
@ToString
@Table(name = "FIELD_VALUE")
public class FieldValue {

  /** The id. */
  @Id
  @SequenceGenerator(name = "field_sequence_generator", sequenceName = "field_sequence",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "field_sequence_generator")
  @Column(name = "ID", columnDefinition = "serial")
  private String id;

  /** The type. */
  @Column(name = "TYPE")
  private String type;

  /** The value. */
  @Column(name = "VALUE")
  private String value;

  /** The id header. */
  @Column(name = "ID_FIELD_SCHEMA")
  private String idFieldSchema;

  /** The record. */
  @ManyToOne
  @JoinColumn(name = "ID_RECORD")
  private RecordValue record;

  /** The field validations. */
  @OneToMany(mappedBy = "fieldValue",
      cascade = {CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH}, orphanRemoval = false)
  private List<FieldValidation> fieldValidations;

  /** The level error. */
  @Transient
  private ErrorTypeEnum levelError;

  /** The Constant DATE_FORMAT. */
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, type, value, idFieldSchema, record);
  }

  /**
   * Double data. That method we use with drools to know if the numer is numeric value avaliable
   *
   * @param value the value
   * @return the double
   */
  public Double doubleData(String value) {
    return Double.parseDouble(value);
  }

  /**
   * Equals.
   *
   * @param obj the o
   *
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final FieldValue field = (FieldValue) obj;
    return id.equals(field.id) && type.equals(field.type) && value.equals(field.value)
        && idFieldSchema.equals(field.idFieldSchema) && record.equals(field.record);
  }

  /**
   * Field and.
   *
   * @param arg1 the arg 1
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public boolean fieldAnd(boolean arg1, boolean arg2) {
    return arg1 && arg2;
  }

  /**
   * Field or.
   *
   * @param arg1 the arg 1
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public boolean fieldOr(boolean arg1, boolean arg2) {
    return arg1 || arg2;
  }

  /**
   * Field not.
   *
   * @param arg the arg
   * @return true, if successful
   */
  public boolean fieldNot(boolean arg) {
    return !arg;
  }

  /**
   * Field number equals.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public boolean fieldNumberEquals(String value, Number arg2) {
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
  public boolean fieldNumberDistinct(String value, Number arg2) {
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
  public boolean fieldNumberGreaterThan(String value, Number arg2) {
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
  public boolean fieldNumberLessThan(String value, Number arg2) {
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
  public boolean fieldNumberGreaterThanOrEqualsThan(String value, Number arg2) {
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
  public boolean fieldNumberLessThanOrEqualsThan(String value, Number arg2) {
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
  public boolean fieldNumberMatches(String value, String regex) {
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
  public String fieldStringLength(String value) {
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
  public boolean fieldStringEquals(String value, String arg2) {
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
  public boolean fieldStringEqualsIgnoreCase(String value, String arg2) {
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
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public boolean fieldStringMatches(String value, String arg2) {
    try {
      return value.matches(arg2);
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
  public boolean fieldDayEquals(String value, Number number) {
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
  public boolean fieldDayDistinct(String value, Number number) {
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
  public boolean fieldDayGreaterThan(String value, Number number) {
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
  public boolean fieldDayLessThan(String value, Number number) {
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
  public boolean fieldDayGreaterThanOrEqualsThan(String value, Number number) {
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
  public boolean fieldDayLessThanOrEqualsThan(String value, Number number) {
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
  public boolean fieldMonthEquals(String value, Number number) {
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
  public boolean fieldMonthDistinct(String value, Number number) {
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
  public boolean fieldMonthGreaterThan(String value, Number number) {
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
  public boolean fieldMonthLessThan(String value, Number number) {
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
  public boolean fieldMonthGreaterThanOrEqualsThan(String value, Number number) {
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
  public boolean fieldMonthLessThanOrEqualsThan(String value, Number number) {
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
  public boolean fieldYearEquals(String value, Number number) {
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
  public boolean fieldYearDistinct(String value, Number number) {
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
  public boolean fieldYearGreaterThan(String value, Number number) {
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
  public boolean fieldYearLessThan(String value, Number number) {
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
  public boolean fieldYearGreaterThanOrEqualsThan(String value, Number number) {
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
  public boolean fieldYearLessThanOrEqualsThan(String value, Number number) {
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
  public boolean fieldDateEquals(String value, String date) {
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
  public boolean fieldDateDistinct(String value, String date) {
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
  public boolean fieldDateGreaterThan(String value, String date) {
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
  public boolean fieldDateLessThan(String value, String date) {
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
  public boolean fieldDateGreaterThanOrEqualsThan(String value, String date) {
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
  public boolean fieldDateLessThanOrEqualsThan(String value, String date) {
    try {
      LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
      LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
      return fieldDate.isBefore(ruleDate) || fieldDate.equals(ruleDate);
    } catch (Exception e) {
      return true;
    }
  }
}
