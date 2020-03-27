package org.eea.dataset.persistence.metabase.domain;

import java.util.Date;
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
 * The type Snapshot.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "SNAPSHOT")
public class Snapshot extends DataSetMetabase {

  /** The id. */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The description. */
  @Column(name = "DESCRIPTION")
  private String description;

  /** The reporting dataset. */
  @ManyToOne
  @JoinColumn(name = "REPORTING_DATASET_ID")
  private ReportingDataset reportingDataset;

  /** The release. */
  @Column(name = "RELEASE")
  private Boolean release;

  /** The date released. */
  @Column(name = "DATE_RELEASED")
  private Date dateReleased;
  /** The blocked. */
  @Column(name = "BLOCKED")
  private Boolean blocked;


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
    final Snapshot snapshot = (Snapshot) o;
    return id.equals(snapshot.id);

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, description, reportingDataset);
  }

}
