package org.eea.interfaces.vo.dataflow;

import lombok.Data;
import org.eea.interfaces.vo.ums.UserNationalCoordinatorVO;

import java.util.List;

/**
 * The Class PaginatedDataflowVO.
 */
@Data
public class PaginatedDataflowWithNationalCoordinatorsVO {

  /** The dataflows. */
  private List<?> dataflows;

  private List<UserNationalCoordinatorVO> nationalCoordinators;

  /** The total records. */
  private Long totalRecords;

  /** The filtered records. */
  private Long filteredRecords;

}
