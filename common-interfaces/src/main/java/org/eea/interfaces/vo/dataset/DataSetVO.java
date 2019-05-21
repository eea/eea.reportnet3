package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class DataSetVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 2680945261242083928L;

  private Long id;
  private String idMongo;
  private String dataSetName;

  /** The table VO. */
  private List<TableVO> tableVO;
  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(dataSetName, id, tableVO);
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
    DataSetVO other = (DataSetVO) obj;
    return Objects.equals(dataSetName, other.dataSetName) && Objects.equals(id, other.id)
        && Objects.equals(tableVO, other.tableVO);
  }



}
