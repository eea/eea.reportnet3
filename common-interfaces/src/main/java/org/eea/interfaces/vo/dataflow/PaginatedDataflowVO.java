package org.eea.interfaces.vo.dataflow;

import java.util.List;
import lombok.Data;

/**
 * The Class PaginatedDataflowVO.
 */
@Data
public class PaginatedDataflowVO {

  /** The dataflows. */
  private List<?> dataflows;

  /** The total records. */
  private Long totalRecords;

  /** The filtered records. */
  private Long filteredRecords;

}
