package org.eea.interfaces.vo.communication;

import java.util.Map;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class UserNotificationContentVO.
 */
@Getter
@Setter
@ToString
public class UserNotificationContentVO {

  /** The dataflow id. */
  private Long dataflowId;

  /** The dataflow name. */
  private String dataflowName;

  /** The provider id. */
  private Long providerId;

  /** The data provider name. */
  private String dataProviderName;

  /** The dataset id. */
  private Long datasetId;

  /** The dataset name. */
  private String datasetName;

  /** The type status. */
  private TypeStatusEnum typeStatus;

  /** The type. */
  private DatasetTypeEnum type;

  /** The custom content. */
  private Map<String, String> customContent;
}
