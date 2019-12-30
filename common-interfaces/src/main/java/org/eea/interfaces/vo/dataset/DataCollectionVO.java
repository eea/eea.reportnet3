package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



/**
 * The Class DataCollectionVO.
 */
@Getter
@Setter
@ToString
public class DataCollectionVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -1348263779137653665L;

  /** The id. */
  private Long id;

  /** The data set name. */
  private String dataSetName;

  /** The creation date. */
  private Date creationDate;

  /** The due date. */
  private Date dueDate;

  /** The status. */
  private String status;

  /** The dataset schema. */
  private String datasetSchema;

  /** The id dataflow. */
  private Long idDataflow;

}
