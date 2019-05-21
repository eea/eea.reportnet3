package org.eea.dataset.persistence.data.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "RECORD")
public class Record {

  @Id
  @Column(name = "ID")
  private Integer id;
  @Column(name = "NAME")
  private String name;
  @Column(name = "ID_TABLE")
  private Long tableValue;
  @Column(name = "ID_PARTITION")
  private Long partitionValue;
  @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<FieldValue> fields;

  @Override
  public int hashCode() {
    return Objects.hash(fields, id, name, partitionValue, tableValue);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Record other = (Record) obj;
    return Objects.equals(fields, other.fields) && Objects.equals(id, other.id)
        && Objects.equals(name, other.name) && Objects.equals(partitionValue, other.partitionValue)
        && Objects.equals(tableValue, other.tableValue);
  }


}
