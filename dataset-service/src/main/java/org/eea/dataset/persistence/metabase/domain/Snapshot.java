package org.eea.dataset.persistence.metabase.domain;

import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Snapshot.
 */
@Entity(name = "SNAPSHOT")
@Getter
@Setter
@ToString
public class Snapshot extends DataSetMetabase {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  @Column(name = "NAME")
  private String name;

  @Column(name = "CREATIONDATE")
  private Date creationDate;

  @Column(name = "DATACOLLECTION_ID")
  private Long datacollection;


  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Snapshot snapshot = (Snapshot) o;
    return id.equals(snapshot.id);

  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, creationDate, datacollection);
  }

}
