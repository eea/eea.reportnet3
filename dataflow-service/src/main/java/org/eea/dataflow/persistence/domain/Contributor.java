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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Contributor.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "CONTRIBUTOR")
public class Contributor {

  /**
   * The id.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /**
   * The email.
   */
  @Column(name = "EMAIL")
  private String email;

  /**
   * The user id.
   */
  @Column(name = "USER_ID")
  private String userId;

  /**
   * The dataflow.
   */
  @ManyToOne
  @JoinColumn(name = "DATAFLOW_ID")
  private Dataflow dataflow;


  /**
   * Equals.
   *
   * @param o the o
   *
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
    final Contributor contributor = (Contributor) o;
    return id.equals(contributor.id);

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, email, userId, dataflow);
  }
}
