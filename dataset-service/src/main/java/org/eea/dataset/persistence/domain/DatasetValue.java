package org.eea.dataset.persistence.domain;

import java.util.List;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.TableVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type DatasetValue.
 */
@Getter
@Setter
@ToString
public class DatasetValue {
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
    DatasetValue other = (DatasetValue) obj;
    return Objects.equals(id, other.id) && Objects.equals(tableVO, other.tableVO);
  }

}
