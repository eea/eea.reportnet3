package org.eea.dataflow.persistence.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Dataflow.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "DATAFLOW")
public class Dataflow {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The name. */
  @Column(name = "NAME")
  private String name;

  /** The description. */
  @Column(name = "DESCRIPTION")
  private String description;

  /** The submission agreement. */
  @OneToOne(mappedBy = "dataflow", cascade = CascadeType.ALL, orphanRemoval = true)
  private SubmissionAgreement submissionAgreement;

  /** The contributors. */
  @OneToMany(mappedBy = "dataflow", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Contributor> contributors;

  /** The documents. */
  @OneToMany(mappedBy = "dataflow", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Document> documents;

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
    final Dataflow dataflow = (Dataflow) o;
    return id.equals(dataflow.id);

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, contributors, submissionAgreement, documents);
  }

}
