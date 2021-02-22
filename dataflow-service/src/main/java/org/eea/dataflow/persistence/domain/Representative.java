package org.eea.dataflow.persistence.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Representative.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "representative")
public class Representative {

  /** The id. */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "representative_id_seq")
  @SequenceGenerator(name = "representative_id_seq", sequenceName = "representative_id_seq",
      allocationSize = 1)
  private Long id;

  /** The dataflow. */
  @ManyToOne
  @JoinColumn(name = "dataflow_id")
  private Dataflow dataflow;

  /** The dataProvider. */
  @ManyToOne
  @JoinColumn(name = "data_provider_id")
  private DataProvider dataProvider;

  /** The lead reporters. */
  @OneToMany(mappedBy = "representative", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<LeadReporter> leadReporters;

  /** The receipt downloaded. */
  @Column(name = "receipt_downloaded")
  private Boolean receiptDownloaded;

  /** The receipt outdated. */
  @Column(name = "receipt_outdated")
  private Boolean receiptOutdated;

  /** The has datasets. */
  @Column(name = "has_datasets")
  private Boolean hasDatasets;


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
    final Representative representative = (Representative) o;
    return id.equals(representative.id);

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, dataflow, dataProvider);
  }
}
