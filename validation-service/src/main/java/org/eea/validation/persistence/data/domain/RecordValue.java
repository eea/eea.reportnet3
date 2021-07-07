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

  /** The data position. */
  @Column(name = "DATA_POSITION", insertable = false, updatable = false)
  private Long dataPosition;

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
}
