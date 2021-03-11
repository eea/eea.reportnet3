package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The type Test Dataset vo.
 */
@Getter
@Setter
@ToString
public class TestDatasetVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -4897594093106180336L;

  /** The id. */
  private Long id;

  /** The data set name. */
  private String dataSetName;

  /** The creation date. */
  private Date creationDate;

  /** The status. */
  private String status;

  /** The dataset schema. */
  private String datasetSchema;

  /** The id dataflow. */
  private Long idDataflow;

}
