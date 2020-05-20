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
   * Number equals.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public boolean numberEquals(String value, Number arg2) {
    return Double.valueOf(value).equals(arg2.doubleValue());
  }

  /**
   * Number distinct.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public boolean numberDistinct(String value, Number arg2) {
    return !Double.valueOf(value).equals(arg2.doubleValue());
  }

  /**
   * Number greater than.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public boolean numberGreaterThan(String value, Number arg2) {
    return Double.valueOf(value) > arg2.doubleValue();
  }

  /**
   * Number less than.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public boolean numberLessThan(String value, Number arg2) {
    return Double.valueOf(value) < arg2.doubleValue();
  }

  /**
   * Number greater than or equals than.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public boolean numberGreaterThanOrEqualsThan(String value, Number arg2) {
    return Double.valueOf(value) >= arg2.doubleValue();
  }

  /**
   * Number less than or equals than.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public boolean numberLessThanOrEqualsThan(String value, Number arg2) {
    return Double.valueOf(value) <= arg2.doubleValue();
  }

  /**
   * Number matches.
   *
   * @param value the value
   * @param regex the regex
   * @return true, if successful
   */
  public boolean numberMatches(String value, String regex) {
    return value.matches(regex);
  }

  /**
   * String length.
   *
   * @param value the value
   * @return the string
   */
  public String stringLength(String value) {
    return "" + value.length();
  }

  /**
   * String equals.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public boolean stringEquals(String value, String arg2) {
    return value.equals(arg2);
  }

  /**
   * String equals ignore case.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public boolean stringEqualsIgnoreCase(String value, String arg2) {
    return value.equalsIgnoreCase(arg2);
  }

  /**
   * String matches.
   *
   * @param value the value
   * @param arg2 the arg 2
   * @return true, if successful
   */
  public boolean stringMatches(String value, String arg2) {
    return value.matches(arg2);
  }

  /**
   * Day equals.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean dayEquals(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getDayOfMonth() == number.longValue();
  }

  /**
   * Day distinct.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean dayDistinct(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getDayOfMonth() != number.longValue();
  }

  /**
   * Day greater than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean dayGreaterThan(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getDayOfMonth() > number.longValue();
  }

  /**
   * Day less than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean dayLessThan(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getDayOfMonth() < number.longValue();
  }

  /**
   * Day greater than or equals than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean dayGreaterThanOrEqualsThan(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getDayOfMonth() >= number.longValue();
  }

  /**
   * Day less than or equals than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean dayLessThanOrEqualsThan(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getDayOfMonth() <= number.longValue();
  }

  /**
   * Month equals.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean monthEquals(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getMonthValue() == number.longValue();
  }

  /**
   * Month distinct.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean monthDistinct(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getMonthValue() != number.longValue();
  }

  /**
   * Month greater than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean monthGreaterThan(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getMonthValue() > number.longValue();
  }

  /**
   * Month less than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean monthLessThan(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getMonthValue() < number.longValue();
  }

  /**
   * Month greater than or equals than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean monthGreaterThanOrEqualsThan(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getMonthValue() >= number.longValue();
  }

  /**
   * Month less than or equals than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean monthLessThanOrEqualsThan(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getMonthValue() <= number.longValue();
  }

  /**
   * Year equals.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean yearEquals(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getYear() == number.longValue();
  }

  /**
   * Year distinct.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean yearDistinct(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getYear() != number.longValue();
  }

  /**
   * Year greater than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean yearGreaterThan(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getYear() > number.longValue();
  }

  /**
   * Year less than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean yearLessThan(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getYear() < number.longValue();
  }

  /**
   * Year greater than or equals than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean yearGreaterThanOrEqualsThan(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getYear() >= number.longValue();
  }

  /**
   * Year less than or equals than.
   *
   * @param value the value
   * @param number the number
   * @return true, if successful
   */
  public boolean yearLessThanOrEqualsThan(String value, Number number) {
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.getYear() <= number.longValue();
  }

  /**
   * Date equals.
   *
   * @param value the value
   * @param date the date
   * @return true, if successful
   */
  public boolean dateEquals(String value, String date) {
    LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.equals(ruleDate);
  }

  /**
   * Date distinct.
   *
   * @param value the value
   * @param date the date
   * @return true, if successful
   */
  public boolean dateDistinct(String value, String date) {
    LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return !fieldDate.equals(ruleDate);
  }

  /**
   * Date greater than.
   *
   * @param value the value
   * @param date the date
   * @return true, if successful
   */
  public boolean dateGreaterThan(String value, String date) {
    LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.isAfter(ruleDate);
  }

  /**
   * Date less than.
   *
   * @param value the value
   * @param date the date
   * @return true, if successful
   */
  public boolean dateLessThan(String value, String date) {
    LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.isBefore(ruleDate);
  }

  /**
   * Date greater than or equals than.
   *
   * @param value the value
   * @param date the date
   * @return true, if successful
   */
  public boolean dateGreaterThanOrEqualsThan(String value, String date) {
    LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.isAfter(ruleDate) || fieldDate.equals(ruleDate);
  }

  /**
   * Date less than or equals than.
   *
   * @param value the value
   * @param date the date
   * @return true, if successful
   */
  public boolean dateLessThanOrEqualsThan(String value, String date) {
    LocalDate ruleDate = LocalDate.parse(date, DATE_FORMAT);
    LocalDate fieldDate = LocalDate.parse(value, DATE_FORMAT);
    return fieldDate.isBefore(ruleDate) || fieldDate.equals(ruleDate);
  }
}
