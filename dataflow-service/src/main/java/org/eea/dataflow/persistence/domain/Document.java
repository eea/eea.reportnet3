package org.eea.dataflow.persistence.domain;

import java.util.Date;
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
 * The type Document.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "DOCUMENT")
public class Document {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The name. */
  @Column(name = "NAME")
  private String name;

  /** The language. */
  @Column(name = "LANGUAGE")
  private String language;

  /** The description. */
  @Column(name = "DESCRIPTION")
  private String description;

  /** The dataflow. */
  @ManyToOne
  @JoinColumn(name = "DATAFLOW_ID")
  private Dataflow dataflow;

  /** The size. */
  @Column(name = "SIZE")
  private Long size;

  /** The date. */
  @Column(name = "DATE")
  private Date date;

  /** The is public. */
  @Column(name = "IS_PUBLIC")
  private Boolean isPublic;

  /** The is big data. */
  @Column(name = "BIG_DATA")
  private Boolean isBigData;

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
    final Document document = (Document) o;
    return id.equals(document.id);

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, name, language, description, dataflow, size, date);
  }
}
