package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.Date;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



/**
 * The Class ReportingDatasetPublicVO.
 */
@Getter
@Setter
@ToString
public class ReportingDatasetPublicVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -1348263779137653665L;

  /** The data set name. */
  private String dataSetName;

  /** The is released. */
  private Boolean isReleased;

  /** The date released. */
  private Date dateReleased;

  /** The data provider id. */
  private Long dataProviderId;

  /** The name dataset schema. */
  private String nameDatasetSchema;

  /** The public file name. */
  private String publicFileName;

  /** The restrict from public. */
  private boolean restrictFromPublic;

  /** The status. */
  private DatasetStatusEnum status;


}
