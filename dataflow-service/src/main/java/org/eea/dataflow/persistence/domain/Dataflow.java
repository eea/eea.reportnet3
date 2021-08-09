package org.eea.dataflow.persistence.domain;

import java.util.Date;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
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
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dataflow_id_seq")
  @SequenceGenerator(name = "dataflow_id_seq", sequenceName = "dataflow_id_seq", allocationSize = 1)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The name. */
  @Column(name = "NAME")
  private String name;

  /** The description. */
  @Column(name = "DESCRIPTION")
  private String description;

  /** The deadline date. */
  @Column(name = "DEADLINE_DATE")
  private Date deadlineDate;

  /** The creation date. */
  @Column(name = "CREATION_DATE")
  private Date creationDate;

  /** The status. */
  @Column(name = "STATUS")
  @Enumerated(EnumType.STRING)
  private TypeStatusEnum status;

  /** The obligation id. */
  @Column(name = "OBLIGATION_ID")
  private Integer obligationId;

  /** The manual acceptance. */
  @Column(name = "MANUAL_ACCEPTANCE")
  private boolean manualAcceptance;

  /** The releasable. */
  @Column(name = "RELEASABLE")
  private boolean releasable;

  /** The show public info. */
  @Column(name = "SHOW_PUBLIC_INFO")
  private boolean showPublicInfo;

  /** The type. */
  @Column(name = "TYPE")
  @Enumerated(EnumType.STRING)
  private TypeDataflowEnum type;

  /** The submission agreement. */
  @OneToOne(mappedBy = "dataflow", cascade = CascadeType.ALL, orphanRemoval = false)
  private SubmissionAgreement submissionAgreement;

  /** The contributors. */
  @OneToMany(mappedBy = "dataflow", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<Contributor> contributors;

  /** The documents. */
  @OneToMany(mappedBy = "dataflow", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<Document> documents;

  /** The weblinks. */
  @OneToMany(mappedBy = "dataflow", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<Weblink> weblinks;

  /** The dataflow representatives. */
  @OneToMany(mappedBy = "dataflow", cascade = CascadeType.ALL, orphanRemoval = false)
  private Set<Representative> representatives;

  /** The integrations. */
  @OneToMany(mappedBy = "dataflow", cascade = CascadeType.ALL, orphanRemoval = false)
  private Set<Integration> integrations;

  /** The data provider group id. */
  @Column(name = "DATAPROVIDER_GROUP_ID")
  private Long dataProviderGroupId;

  /** The fme user. */
  @Column(name = "FME_USER_ID")
  private Long fmeUserId;

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
