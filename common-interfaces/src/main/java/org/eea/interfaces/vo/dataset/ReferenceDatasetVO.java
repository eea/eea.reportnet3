package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



/**
 * The Class ReferenceDatasetVO.
 */
@Getter
@Setter
@ToString
public class ReferenceDatasetVO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 2330536469877243680L;

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


  /** The public file name. */
  private String publicFileName;

  /** The updatable. */
  private Boolean updatable;

}
