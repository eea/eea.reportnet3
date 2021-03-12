package org.eea.interfaces.vo.dataflow;

import java.util.List;
import lombok.Data;

@Data
public class DataflowPublicPaginatedVO {

  /** The dataflows public. */
  List<DataflowPublicVO> publicDataflows;

  /** The total records. */
  private Long totalRecords;

}
