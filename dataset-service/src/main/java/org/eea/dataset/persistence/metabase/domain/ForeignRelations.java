package org.eea.dataset.persistence.metabase.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



@Entity
@Getter
@Setter
@ToString
@Table(name = "FOREIGN_RELATIONS")
public class ForeignRelations {

  @Id
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "DATASET_ID")
  private DataSetMetabase idDatasetDestination;

  @Column(name = "ID_PK")
  private String idPk;

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
    final ForeignRelations relation = (ForeignRelations) o;
    return idDatasetDestination.equals(relation.idDatasetDestination) && idPk.equals(relation.idPk);

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(idDatasetDestination, idPk);
  }

}
