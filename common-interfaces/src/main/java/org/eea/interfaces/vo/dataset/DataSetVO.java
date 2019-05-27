package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class DataSetVO.
 */
@Getter
@Setter
@ToString
public class DataSetVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 2680945261242083928L;

  /** The id. */
  private Long id;

  /** The id mongo. */
  private String idMongo;

  /** The table VO. */
  private List<TableVO> tableVO;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, tableVO, idMongo);
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
    DataSetVO other = (DataSetVO) obj;
    return Objects.equals(id, other.id) && Objects.equals(tableVO, other.tableVO)
        && Objects.equals(idMongo, other.idMongo);
  }



}
