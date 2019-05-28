/**
 * 
 */
package org.eea.dataset.persistence.metabase.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class TableStructure.
 *
 * @author Mario Severa
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "TABLE_COLLECTION")
public class TableCollection {


  /** The Id. */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  /** The Dataflow id. */
  @Column(name = "DATAFLOW_ID")
  private Long dataFlowId;

  /** The Data set id. */
  @Column(name = "DATASET_ID")
  private Long dataSetId;

  /** The Table name. */
  @Column(name = "TABLE_NAME")
  private String tableName;

  /** The table headers collections. */
  @OneToMany(mappedBy = "tableId", cascade = CascadeType.ALL, fetch = FetchType.EAGER,
      orphanRemoval = false)
  private List<TableHeadersCollection> tableHeadersCollections;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(dataSetId, dataFlowId, id, tableName, tableHeadersCollections);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TableCollection other = (TableCollection) obj;
    return Objects.equals(dataSetId, other.dataSetId)
        && Objects.equals(dataFlowId, other.dataFlowId) && Objects.equals(id, other.id)
        && Objects.equals(tableName, other.tableName)
        && Objects.equals(tableHeadersCollections, other.tableHeadersCollections);
  }

}
