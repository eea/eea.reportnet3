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


/**
 * The Class SnapshotSchema.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "SNAPSHOT_SCHEMA")
public class SnapshotSchema extends DataSetMetabase {

  /** The id. */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The description. */
  @Column(name = "DESCRIPTION")
  private String description;


  /** The design dataset. */
  @ManyToOne
  @JoinColumn(name = "DESIGN_DATASET_ID")
  private DesignDataset designDataset;



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
    final SnapshotSchema snapshot = (SnapshotSchema) o;
    return id.equals(snapshot.id);

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, description, designDataset);
  }

}
