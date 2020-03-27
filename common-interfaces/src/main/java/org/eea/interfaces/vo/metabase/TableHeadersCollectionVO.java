/**
 * 
 */
package org.eea.interfaces.vo.metabase;

import java.io.Serializable;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.enums.DataType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class TableHeadersCollectionVO.
 */
@Getter
@Setter
@ToString
public class TableHeadersCollectionVO implements Serializable {


  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 6135503787557145245L;


  /** The header name. */
  private String headerName;

  /** The header type. */
  private DataType headerType;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(headerName, headerType);
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
    TableHeadersCollectionVO other = (TableHeadersCollectionVO) obj;
    return Objects.equals(headerName, other.headerName)
        && Objects.equals(headerType, other.headerType);
  }

}
