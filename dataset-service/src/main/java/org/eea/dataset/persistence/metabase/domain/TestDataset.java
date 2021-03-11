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
 * The Class TestDataset.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "TEST_DATASET")
public class TestDataset extends DataSetMetabase {

  /** The id. */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;


  /**
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final TestDataset testDataset = (TestDataset) o;
    return id.equals(testDataset.id);

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

}
