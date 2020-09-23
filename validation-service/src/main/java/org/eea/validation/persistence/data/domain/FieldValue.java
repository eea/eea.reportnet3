package org.eea.validation.persistence.data.domain;

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
}
