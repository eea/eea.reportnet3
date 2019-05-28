package org.eea.collaboration.persistence.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Comment.
 */
@Entity

/**
 * Gets the dataset id.
 *
 * @return the dataset id
 */
@Getter

/**
 * Sets the dataset id.
 *
 * @param datasetId the new dataset id
 */
@Setter

/**
 * To string.
 *
 * @return the java.lang. string
 */
@ToString
@Table(name = "COMMENT")
public class Comment {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The content. */
  @Column(name = "CONTENT")
  private String content;

  /** The dataflow id. */
  @Column(name = "DATAFLOW_ID")
  private String dataflowId;

  /** The dataset id. */
  @Column(name = "DATASET_ID")
  private String datasetId;

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
    final Comment comment = (Comment) o;
    return id.equals(comment.id);

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, content, dataflowId, datasetId);
  }
}
