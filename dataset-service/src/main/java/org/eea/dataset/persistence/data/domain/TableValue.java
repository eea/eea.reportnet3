package org.eea.dataset.persistence.data.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class TableValue.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "TABLE_VALUE")
public class TableValue {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The name. */
  @Column(name = "NAME")
  private String name;

  /** The id mongo. */
  @Column(name = "ID_MONGO")
  private String idMongo;

  /** The records. */
  @OneToMany(mappedBy = "tableValue", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<RecordValue> records;

  /** The dataset id. */
  @ManyToOne
  @JoinColumn(name = "DATASET_ID", insertable = false, updatable = false)
  private DatasetValue datasetId;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, name, records, idMongo, datasetId);
  }

  /**
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    TableValue table = (TableValue) obj;
    return id.equals(table.id) && name.equals(table.name);
  }

}
