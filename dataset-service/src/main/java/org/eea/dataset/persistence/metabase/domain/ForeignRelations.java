package org.eea.dataset.persistence.metabase.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



/**
 * The Class ForeignRelations.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "FOREIGN_RELATIONS")
public class ForeignRelations {

  /** The id. */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "foreign_relations_id_seq")
  @SequenceGenerator(name = "foreign_relations_id_seq", sequenceName = "foreign_relations_id_seq",
      allocationSize = 1)
  private Long id;

  /** The id dataset origin. */
  @ManyToOne
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "DATASET_ID_ORIGIN")
  private DataSetMetabase idDatasetOrigin;

  /** The id dataset destination. */
  @ManyToOne
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "DATASET_ID_DESTINATION")
  private DataSetMetabase idDatasetDestination;

  /** The id pk. */
  @Column(name = "ID_PK")
  private String idPk;

  /** The id fk origin. */
  @Column(name = "ID_FK_ORIGIN")
  private String idFkOrigin;

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
    return idDatasetDestination.equals(relation.idDatasetDestination) && idPk.equals(relation.idPk)
        && idDatasetOrigin.equals(relation.idDatasetOrigin)
        && idFkOrigin.equals(relation.idFkOrigin);

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(idDatasetDestination, idDatasetOrigin, idPk, idFkOrigin);
  }

}
