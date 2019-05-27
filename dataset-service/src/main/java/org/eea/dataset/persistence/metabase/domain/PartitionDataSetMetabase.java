/**
 * 
 */
package org.eea.dataset.persistence.metabase.domain;

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
 * @author Mario Severa
 *
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "PARTITION_DATASET")
public class PartitionDataSetMetabase {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The id data set. */
  @ManyToOne
  @JoinColumn(name = "ID_DATASET")
  private DataSetMetabase idDataSet;

  /** The username. */
  @Column(name = "USER_NAME")
  private String username;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, idDataSet, username);
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
    PartitionDataSetMetabase other = (PartitionDataSetMetabase) obj;
    return Objects.equals(id, other.id) && Objects.equals(idDataSet, other.idDataSet)
        && Objects.equals(username, other.username);
  }



}
