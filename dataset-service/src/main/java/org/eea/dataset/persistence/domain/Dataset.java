package org.eea.dataset.persistence.domain;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Dataset.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "DATASET")
public class Dataset {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  @Column(name = "id_dataset", columnDefinition = "serial")
  private String idDataSet;

  @Column(name = "NAME")
  private String name;

  @Column(name = "DATE_CREATION")
  private Date creationDate;

  @Column(name = "VISIBILITY")
  private String visibility;

  @Column(name = "URL_CONNECTION")
  private String urlConnection;

  @Column(name = "STATUS")
  private String status;

  @Nonnull
  @Column(name = "DATAFLOW_ID")
  private Long dataflowId;

  @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<DataSetPartition> dataSetPartitions;

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Dataset dataset = (Dataset) o;
    return id.equals(dataset.id);

  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, creationDate, visibility, urlConnection, status, dataflowId,
        dataSetPartitions);
  }

}
