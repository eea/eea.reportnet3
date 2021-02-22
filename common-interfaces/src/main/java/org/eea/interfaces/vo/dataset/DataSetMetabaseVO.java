package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.Date;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class DataSetMetabaseVO.
 */
@Getter
@Setter
@ToString
public class DataSetMetabaseVO implements Serializable {

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = -1348263779137653665L;

  /**
   * The id.
   */
  private Long id;

  /**
   * The data set name.
   */
  private String dataSetName;

  /**
   * The creation date.
   */
  private Date creationDate;

  /**
   * The Dataflow id.
   */
  private Long dataflowId;

  /**
   * The dataset schema.
   */
  private String datasetSchema;

  /**
   * The data provider id.
   */
  private Long dataProviderId;

  /**
   * The dataset type enum
   */
  private DatasetTypeEnum datasetTypeEnum;

  /** The status. */
  private DatasetStatusEnum status;

  /** The available in public. */
  private boolean availableInPublic;



}
