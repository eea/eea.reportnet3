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
@Getter
@Setter
@ToString
public class DataFlowVO implements Serializable {

  private static final long serialVersionUID = -8073212422480973637L;
  private Long id;
  private List<DataSetVO> datasets;

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

  @Override
  public int hashCode() {
    return Objects.hash(id, datasets);
  }

}
