package org.eea.interfaces.vo.rod;

import lombok.Data;
import java.util.List;

/**
 * The Class PaginatedObligationVO.
 */
@Data
public class PaginatedObligationVO {

  /** The obligations. */
  private List<ObligationWithDataflowsVO> obligations;

  /** The total records. */
  private Long totalRecords;

  /** The filtered records. */
  private Long filteredRecords;
}