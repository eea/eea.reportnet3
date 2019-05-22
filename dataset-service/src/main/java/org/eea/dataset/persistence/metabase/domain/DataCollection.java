package org.eea.dataset.persistence.metabase.domain;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type DataCollection.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "DATA_COLLECTION")
public class DataCollection extends DataSetMetabase {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  @Column(name = "NAME")
  private String name;

  @Column(name = "VISIBLE")
  private Boolean visible;

  @Column(name = "DUEDATE")
  private Date dueDate;

  @OneToMany(mappedBy = "datacollection", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Snapshot> snapshots;



  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final DataCollection dataCollection = (DataCollection) o;
    return id.equals(dataCollection.id);

  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, visible, dueDate, snapshots);
  }

}
