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

@Getter
@Setter
@ToString
@Entity
@Table(name = "TABLE_HEADERS_COLLECTION")
public class TableHeadersCollection {


  /** The Id. */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  /** The table collection id. */
  @ManyToOne
  @JoinColumn(name = "ID_TABLE")
  private TableCollection tableId;

  /** The header name. */
  @Column(name = "HEADER_NAME")
  private String headerName;

  /** The header type. */
  @Column(name = "HEADER_TYPE")
  private String headerType;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, headerName, headerType, tableId);
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
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    TableHeadersCollection other = (TableHeadersCollection) obj;
    return Objects.equals(id, other.id) && Objects.equals(headerName, other.headerName)
        && Objects.equals(headerType, other.headerType) && Objects.equals(tableId, other.tableId);
  }



}
