package org.eea.interfaces.vo.recordstore;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class ProcessesVO.
 */

@Getter
@Setter
@ToString
public class ProcessesVO {

  /** The process list. */
  private List<ProcessVO> processList;

  /** The total records. */
  private Long totalRecords;

  /** The filtered records. */
  private Long filteredRecords;

}
