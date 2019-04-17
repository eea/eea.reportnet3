package org.eea.dataflow.persistence.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * The type Submission agreement.
 */
@Entity
@Table(name = "SUBMISSION_AGREEMENT")
public class SubmissionAgreement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;
  @Column(name = "name")
  private String name;
  @Column(name = "description")
  private String description;
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "dataflow_id", referencedColumnName = "id")
  private Dataflow dataflow;

  /**
   * Gets id.
   *
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * Sets id.
   *
   * @param id the id
   */
  public void setId(final Long id) {
    this.id = id;
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets name.
   *
   * @param name the name
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Gets description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets description.
   *
   * @param description the description
   */
  public void setDescription(final String description) {
    this.description = description;
  }

  /**
   * Gets dataflow.
   *
   * @return the dataflow
   */
  public Dataflow getDataflow() {
    return dataflow;
  }

  /**
   * Sets dataflow.
   *
   * @param dataflow the dataflow
   */
  public void setDataflow(final Dataflow dataflow) {
    this.dataflow = dataflow;
  }
}
