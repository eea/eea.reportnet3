package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TableVO implements Serializable {

  private static final long serialVersionUID = 2680945261242083928L;

  private String id;
  private List<RecordVO> records;
  private List<FieldSchemaVO> headers;
  private String name;



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
    final TableVO dataSetVO = (TableVO) o;
    return id.equals(dataSetVO.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, records);
  }

}
