package org.eea.dataset.persistence.metabase.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type ReportingDataset.
 */
@Entity(name = "REPORTING_DATASET")
@Getter
@Setter
@ToString
public class ReportingDataset extends DataSetMetabase {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;



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

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

}
