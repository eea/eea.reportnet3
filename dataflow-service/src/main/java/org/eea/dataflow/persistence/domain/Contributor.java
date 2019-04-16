package org.eea.dataflow.persistence.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The type Contributor.
 */
@Entity
@Table(name = "CONTRIBUTOR")
public class Contributor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;
  @Column(name = "EMAIL")
  private String email;
  @Column(name = "USER_ID")
  private Long userId;
  @ManyToOne
  @JoinColumn(name = "DATAFLOW_ID")
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
   * Gets email.
   *
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets email.
   *
   * @param email the email
   */
  public void setEmail(final String email) {
    this.email = email;
  }

  /**
   * Gets user id.
   *
   * @return the user id
   */
  public Long getUserId() {
    return userId;
  }

  /**
   * Sets user id.
   *
   * @param userId the user id
   */
  public void setUserId(final Long userId) {
    this.userId = userId;
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

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Contributor that = (Contributor) o;
    return id.equals(that.id) &&
        email.equals(that.email) &&
        userId.equals(that.userId) &&
        dataflow.equals(that.dataflow);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, email, userId, dataflow);
  }

  @Override
  public String toString() {
    return "Contributor{" +
        "id=" + id +
        ", email='" + email + '\'' +
        ", userId=" + userId +
        ", dataflow=" + dataflow +
        '}';
  }
}
