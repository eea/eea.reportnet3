package org.eea.interfaces.vo.recordstore;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class ProcessVO.
 */
@Getter
@Setter
@ToString
public class ProcessVO {

  /** The id. */
  private Long id;

  /** The dataflow id. */
  private Long dataflowId;

  /** The dataset id. */
  private Long datasetId;

  /** The dataflow name. */
  private String dataflowName;

  /** The dataset name. */
  private String datasetName;

  /** The status. */
  private String status;

  /** The queued date. */
  private Date queuedDate;

  /** The process starting date. */
  private Date processStartingDate;

  /** The process finishing date. */
  private Date processFinishingDate;

  /** The user. */
  private String user;

  /** The priority. */
  private int priority;

  /** The released. */
  private boolean released;

  /** The process id. */
  private String processId;

}
