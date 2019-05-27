package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.DataSetVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Data flow vo.
 */

/**
 * Gets the datasets.
 *
 * @return the datasets
 */
@Getter

/**
 * Sets the datasets.
 *
 * @param datasets the new datasets
 */
@Setter

/**
 * To string.
 *
 * @return the java.lang. string
 */
@ToString
public class DataFlowVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -8073212422480973637L;

  /** The id. */
  private Long id;

  /** The datasets. */
  private List<DataSetVO> datasets;

  /**
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final DataFlowVO that = (DataFlowVO) o;
    return id.equals(that.id);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, datasets);
  }

}
