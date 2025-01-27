package org.eea.interfaces.vo.dataflow;

import lombok.Data;
import org.eea.interfaces.vo.ums.UserNationalCoordinatorVO;

import java.io.Serializable;
import java.util.List;

/**
 * The Class PaginatedDataflowVO.
 */
@Data
public class PaginatedDataflowWithNationalCoordinatorsVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -1348263779137653665L;

  /** The dataflows. */
  private List<DataflowInternalVO> dataflows;

  /** The national coordinators. */
  private List<UserNationalCoordinatorVO> nationalCoordinators;

  /** The total records. */
  private Long totalRecords;

  /** The filtered records. */
  private Long filteredRecords;

}
