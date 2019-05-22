package org.eea.dataset.persistence.data.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class FieldValue.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "FIELD_VALUE")
public class FieldValue {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The type. */
  @Column(name = "TYPE")
  private String type;

  /** The value. */
  @Column(name = "VALUE")
  private String value;

  /** The id header. */
  @Column(name = "ID_FIELDSCHEMA")
  private String idFieldSchema;

  /** The record. */
  @ManyToOne
  @JoinColumn(name = "ID_RECORD")
  private RecordValue record;


  /**
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FieldValue field = (FieldValue) o;
    return id.equals(field.id) && type.equals(field.type) && value.equals(field.value);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, type, value);
  }

}
