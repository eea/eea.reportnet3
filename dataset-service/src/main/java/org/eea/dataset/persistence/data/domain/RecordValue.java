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
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.eea.dataset.persistence.data.SortFieldsHelper;

/**
 * The Class Record.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "RECORD_VALUE")
public class RecordValue {

  @PostLoad
  public void initSortFields() {
    String sortingField = SortFieldsHelper.getSortingField();
    if (sortingField != null && !sortingField.isEmpty()) {
      this.sortCriteria = fields.stream()
          .filter(field -> field.getIdFieldSchema().equals(sortingField)).findFirst().get()
          .getValue();
    }
  }

  /**
   * The id.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /**
   * The id mongo.
   */
  @Column(name = "ID_RECORD_SCHEMA")
  private String idRecordSchema;

  /**
   * The id partition.
   */
  @Column(name = "DATASET_PARTITION_ID")
  private Long datasetPartitionId;

  /**
   * The table value.
   */
  @ManyToOne
  @JoinColumn(name = "ID_TABLE")
  private TableValue tableValue;

  /**
   * The fields.
   */
  @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<FieldValue> fields;

  @Transient
  private String sortCriteria;

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
   *
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
        && Objects.equals(idRecordSchema, other.idRecordSchema) && Objects
        .equals(tableValue, other.tableValue);
  }


}
