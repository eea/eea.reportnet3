package org.eea.dataset.persistence.metabase.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type ReportingDataset.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "REPORTING_DATASET")
public class ReportingDataset extends DataSetMetabase {

  /** The id. */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  @OneToMany(mappedBy = "reportingDataset", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Snapshot> snapshots;


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
    final ReportingDataset reportingDataset = (ReportingDataset) o;
    return id.equals(reportingDataset.id);

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
