package org.eea.dataset.persistence.metabase.domain;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import lombok.Data;

/**
 * Instantiates a new data set metabase.
 */

/**
 * Instantiates a new data set metabase.
 */
@Data
@Entity
@Table(name = "DATASET")
@Inheritance(strategy = InheritanceType.JOINED)
public class DataSetMetabase {

  /** The id. */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dataset_id_seq")
  @SequenceGenerator(name = "dataset_id_seq", sequenceName = "dataset_id_seq", allocationSize = 1)
  private Long id;

  /** The data set name. */
  @Column(name = "DATASET_NAME")
  private String dataSetName;

  /** The dataflow id. */
  @Column(name = "DataflowId")
  private Long dataflowId;

  /** The creation date. */
  @Column(name = "DATE_CREATION")
  private Date creationDate;

  /** The visibility. */
  @Column(name = "VISIBILITY")
  private String visibility;

  /** The url connection. */
  @Column(name = "URL_CONNECTION")
  private String urlConnection;

  /** The status. */
  @Column(name = "STATUS")
  @Enumerated(EnumType.STRING)
  private DatasetStatusEnum status;

  /** The dataset schema. */
  @Column(name = "DATASET_SCHEMA")
  private String datasetSchema;

  /** The data provider id. */
  @Column(name = "DATA_PROVIDER_ID")
  private Long dataProviderId;

  /** The releasing. */
  @Column(name = "RELEASING")
  private Boolean releasing;

  /** The isrestricted. */
  @Column(name = "ISRESTRICTED")
  private Boolean isrestricted;

  /** The partitions. */
  @OneToMany(mappedBy = "idDataSet", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<PartitionDataSetMetabase> partitions;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(dataSetName, id, creationDate, visibility, urlConnection, status,
        partitions);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    DataSetMetabase other = (DataSetMetabase) obj;
    return Objects.equals(dataSetName, other.dataSetName) && Objects.equals(id, other.id)
        && Objects.equals(partitions, other.partitions);
  }
}
