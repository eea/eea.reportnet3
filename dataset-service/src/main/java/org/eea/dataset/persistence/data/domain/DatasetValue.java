package org.eea.dataset.persistence.data.domain;

import java.util.List;
import java.util.Objects;
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
@Table(name = "DATASET_VALUE")
public class DatasetValue {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The id mongo. */
  @Column(name = "ID_MONGO")
  private String idMongo;

  /** The table values. */
  @OneToMany(mappedBy = "datasetId", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<TableValue> tableValues;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, tableValues, idMongo);
  }

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
    return id.equals(dataset.id) && idMongo.equals(dataset.idMongo)
        && tableValues.equals(dataset.tableValues);
  }

}
