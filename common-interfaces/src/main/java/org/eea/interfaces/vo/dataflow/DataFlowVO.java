package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.document.DocumentVO;
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

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -8073212422480973637L;

  /** The id. */
  private Long id;

  /** The datasets. */
  private List<DataSetVO> datasets;

  private String description;

  private String name;

  private Date deadlineDate;

  private TypeStatusEnum status;

  private List<DocumentVO> documents;

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
    return Objects.hash(id, description, name, deadlineDate, status, datasets);
  }

}
