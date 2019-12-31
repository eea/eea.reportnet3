package org.eea.interfaces.vo.dataflow;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The class DataProviderCodeVO.
 */
@Getter
@Setter
@ToString
public class DataProviderCodeVO {


  /** The data provider group id. */
  private Long dataProviderGroupId;


  /** The label. */
  private String label;

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
    final DataProviderCodeVO that = (DataProviderCodeVO) o;
    return dataProviderGroupId.equals(that.dataProviderGroupId);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(dataProviderGroupId, label);
  }
}
