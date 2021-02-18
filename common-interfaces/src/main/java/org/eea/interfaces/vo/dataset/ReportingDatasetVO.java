package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.Date;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class ReportingDatasetVO.
 */
@Getter
@Setter
@ToString
public class ReportingDatasetVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -1348263779137653665L;

  /** The id. */
  private Long id;

  /** The data set name. */
  private String dataSetName;

  /** The creation date. */
  private Date creationDate;

  /** The is released. */
  private Boolean isReleased;

  /** The date released. */
  private Date dateReleased;

  /** The data provider id. */
  private Long dataProviderId;

  /** The dataset schema. */
  private String datasetSchema;

  /** The name dataset schema. */
  private String nameDatasetSchema;

  /** The status. */
  private DatasetStatusEnum status;

  /** The releasing. */
  private Boolean releasing;

  /** The available in public. */
  private boolean availableInPublic;

}
