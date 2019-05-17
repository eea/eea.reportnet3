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

  private static final long serialVersionUID = 2680945261242083928L;

  private String id;
  private List<TableVO> tableVO;



  @Override
  public int hashCode() {
    return Objects.hash(id, tableVO);
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DataSetVO other = (DataSetVO) obj;
    return Objects.equals(id, other.id) && Objects.equals(tableVO, other.tableVO);
  }



}
