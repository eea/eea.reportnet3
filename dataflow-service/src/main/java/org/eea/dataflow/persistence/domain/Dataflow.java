package org.eea.dataflow.persistence.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
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

  @Column(name = "DEADLINE_DATE")
  private Date deadlineDate;

  @Column(name = "CREATION_DATE")
  private Date creationDate;

  @Column(name = "STATUS")
  @Enumerated(EnumType.STRING)
  private TypeStatusEnum status;

  /** The submission agreement. */
  @OneToOne(mappedBy = "dataflow", cascade = CascadeType.ALL, orphanRemoval = true)
  private SubmissionAgreement submissionAgreement;

  /** The contributors. */
  @OneToMany(mappedBy = "dataflow", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Contributor> contributors;

  /** The documents. */
  @OneToMany(mappedBy = "dataflow", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Document> documents;

  @OneToMany(mappedBy = "dataflow", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Weblink> weblinks;

  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "dataflow_user_request", joinColumns = @JoinColumn(name = "dataflow_id"),
      inverseJoinColumns = @JoinColumn(name = "user_request_id"))
  private Set<UserRequest> userRequests = new HashSet<>();

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
