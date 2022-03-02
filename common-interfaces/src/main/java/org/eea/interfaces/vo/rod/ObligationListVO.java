package org.eea.interfaces.vo.rod;

import java.util.List;
import lombok.Data;

@Data
public class ObligationListVO {

  /** The obligations. */
  private List<ObligationVO> obligations;

  /** The total records. */
  private Long totalRecords;

  /** The filtered records. */
  private Long filteredRecords;
}
