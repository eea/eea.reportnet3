package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.ReferenceDatasetPublicVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetPublicVO;
import org.eea.interfaces.vo.rod.ObligationVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Data flow vo.
 */

@Getter
@Setter
@ToString
public class DataflowPublicVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -8073212422480973637L;

  /** The id. */
  private Long id;

  /** The datasets. */
  private List<ReportingDatasetPublicVO> reportingDatasets;

  /** The reference datasets. */
  private List<ReferenceDatasetPublicVO> referenceDatasets;

  /** The description. */
  private String description;

  /** The name. */
  private String name;

  /** The deadline date. */
  private Date deadlineDate;

  /** The obligation. */
  private ObligationVO obligation;

  /** The status. */
  private TypeStatusEnum status;

  /** The releasable. */
  private boolean releasable;

  /** The type. */
  private TypeDataflowEnum type;


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
    final DataflowPublicVO that = (DataflowPublicVO) o;
    return id.equals(that.id);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, description, name, deadlineDate, reportingDatasets, referenceDatasets,
        obligation, status, releasable);
  }

}
