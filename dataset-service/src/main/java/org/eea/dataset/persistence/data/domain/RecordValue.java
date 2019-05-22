package org.eea.dataset.persistence.data.domain;

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
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Record.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "RECORD")
public class RecordValue {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @Column(name = "ID", columnDefinition = "serial")
  private Integer id;

  /** The id mongo. */
  @Column(name = "ID_MONGO")
  private String idMongo;

  /** The table value. */
  @ManyToOne
  @JoinColumn(name = "ID_TABLE")
  private TableValue tableValue;

  /** The fields. */
  @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<FieldValue> fields;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(fields, id, idMongo, tableValue);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RecordValue other = (RecordValue) obj;
    return Objects.equals(fields, other.fields) && Objects.equals(id, other.id)
        && Objects.equals(idMongo, other.idMongo) && Objects.equals(tableValue, other.tableValue);
  }


}
