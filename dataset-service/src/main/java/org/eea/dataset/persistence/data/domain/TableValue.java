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
import org.hibernate.annotations.DynamicUpdate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class TableValue.
 */
@Entity
@Getter
@Setter
@ToString
@DynamicUpdate
@Table(name = "TABLE_VALUE")
public class TableValue {

  /**
   * The id.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /**
   * The name.
   */
  @Column(name = "NAME")
  private String name;

  /**
   * The id mongo.
   */
  @Column(name = "ID_TABLE_SCHEMA")
  private String idTableSchema;

  /**
   * The records.
   */
  @OneToMany(mappedBy = "tableValue", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<RecordValue> records;

  /** The table validations. */
  @OneToMany(mappedBy = "idTable", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<TableValidation> tableValidations;

  /**
   * The dataset id.
   */
  @ManyToOne
  @JoinColumn(name = "DATASET_ID")
  private DatasetValue datasetId;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, name, records, idTableSchema, datasetId);
  }

  /**
   * Equals.
   *
   * @param o the o
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
    final TableValue table = (TableValue) obj;
    return id.equals(table.id) && name.equals(table.name);
  }

}
