package org.eea.dataset.persistence.metabase.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class ReferenceDataset.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "REFERENCE_DATASET")
public class ReferenceDataset extends DataSetMetabase {

  /** The id. */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;


  /** The updatable. */
  @Column(name = "UPDATABLE")
  private Boolean updatable;

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
    final ReferenceDataset referenceDataset = (ReferenceDataset) object;
    return id.equals(referenceDataset.id) && updatable.equals(referenceDataset.updatable);

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, updatable);
  }

}
