/**
 * 
 */
package org.eea.dataflow.persistence.domain;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class DataSetMetabase.
 *
 * @author Mario Severa
 */


@Getter
@Setter
@ToString
@Entity
@Table(name = "DATASET")
@Inheritance(strategy = InheritanceType.JOINED)
public class DataSetMetabase {

  /** The id. */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  /** The data set name. */
  @Column(name = "DATASET_NAME")
  private String dataSetName;

  /** The creation date. */
  @Column(name = "DATE_CREATION")
  private Date creationDate;

  /** The visibility. */
  @Column(name = "VISIBILITY")
  private String visibility;

  /** The url connection. */
  @Column(name = "URL_CONNECTION")
  private String urlConnection;

  /** The status. */
  @Column(name = "STATUS")
  private String status;

  /** The partitions. */
  @OneToMany(mappedBy = "idDataSet", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<PartitionDataSetMetabase> partitions;


  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(dataSetName, id, creationDate, visibility, urlConnection, status,
        partitions);
  }

  /**
   * Equals.
   *
   * @param obj the obj
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
    DataSetMetabase other = (DataSetMetabase) obj;
    return Objects.equals(dataSetName, other.dataSetName) && Objects.equals(id, other.id)
        && Objects.equals(partitions, other.partitions);
  }



}
