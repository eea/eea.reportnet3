package org.eea.dataflow.persistence.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Weblink.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "WEBLINK")
public class Weblink {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "weblink_id_seq")
  @SequenceGenerator(name = "weblink_id_seq", sequenceName = "weblink_id_seq", allocationSize = 1)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The name. */
  @Column(name = "DESCRIPTION")
  private String description;

  /** The url. */
  @Column(name = "URL")
  private String url;

  /** The dataflow. */
  @ManyToOne
  @JoinColumn(name = "DATAFLOW_ID")
  private Dataflow dataflow;

  /** The is public. */
  @Column(name = "IS_PUBLIC")
  private Boolean isPublic;

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
    final Weblink contributor = (Weblink) o;
    return id.equals(contributor.id);

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, description, url, dataflow);
  }
}
