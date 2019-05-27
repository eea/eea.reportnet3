/**
 * 
 */
package org.eea.dataset.metabase.domain;

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
 * @author Mario Severa
 *
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "DataSet")
public class DataSetMetabase {

  @Id
  @Column(name = "ID", columnDefinition = "serial")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;
  @Column(name = "DataSetName")
  private Integer dataSetName;

  @OneToMany(mappedBy = "idDataSet", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<PartitionDataSetMetabase> partitions;

  @Override
  public int hashCode() {
    return Objects.hash(dataSetName, id, partitions);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DataSetMetabase other = (DataSetMetabase) obj;
    return Objects.equals(dataSetName, other.dataSetName) && Objects.equals(id, other.id)
        && Objects.equals(partitions, other.partitions);
  }



}
