package org.eea.dataflow.persistence.domain;

import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
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
  @ManyToMany(cascade = {CascadeType.MERGE}, fetch = FetchType.EAGER)
  @JoinTable(name = "representative_user", joinColumns = @JoinColumn(name = "representative_id"),
      inverseJoinColumns = @JoinColumn(name = "user_mail"))
  private Set<User> reporters;

  /** The receipt downloaded. */
  @Column(name = "receipt_downloaded")
  private Boolean receiptDownloaded;

  /** The receipt outdated. */
  @Column(name = "receipt_outdated")
  private Boolean receiptOutdated;

  /** The has datasets. */
  @Column(name = "has_datasets")
  private Boolean hasDatasets;

  /** The restrict from public. */
  @Column(name = "restrict_from_public")
  private boolean restrictFromPublic;

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
