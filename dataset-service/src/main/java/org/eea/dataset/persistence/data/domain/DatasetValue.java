package org.eea.dataset.persistence.data.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
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
@Table(name = "DATASET_VALUE")
@Inheritance(strategy = InheritanceType.JOINED)
public class DatasetValue {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The id mongo. */
  @Column(name = "ID_MONGO")
  private String idMongo;

  /** The data set name. */
  @Column(name = "DATASET_NAME")
  private String dataSetName;

  /** The table values. */
  @OneToMany(mappedBy = "datasetId", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<TableValue> tableValues;

  @Column(name = "DATASET_METABASE_ID")
  private Long datasetMetabaseId;

  /**
   * Equals.
   *
   * @param object the object
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }
    final DatasetValue dataset = (DatasetValue) object;
    return id.equals(dataset.id);

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, tableValues);
  }

}
