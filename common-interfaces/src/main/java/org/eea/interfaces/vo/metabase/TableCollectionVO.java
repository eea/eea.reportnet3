package org.eea.interfaces.vo.metabase;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class TableCollectionVO.
 */
@Getter
@Setter
@ToString
public class TableCollectionVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 4367786920962720894L;


  /** The Table name. */
  private String tableName;

  /** The table headers collections. */
  private List<TableHeadersCollectionVO> tableHeadersCollections;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(tableHeadersCollections, tableName);
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
    TableCollectionVO other = (TableCollectionVO) obj;
    return Objects.equals(tableHeadersCollections, other.tableHeadersCollections)
        && Objects.equals(tableName, other.tableName);
  }


}
