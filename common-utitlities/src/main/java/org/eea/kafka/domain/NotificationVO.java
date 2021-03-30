package org.eea.kafka.domain;

import java.io.Serializable;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The class that works as interface to fulfill notifiable events.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class NotificationVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 6919930391293266556L;

  /** The user. */
  private String user;

  /** The dataset id. */
  private Long datasetId;

  /** The dataset name. */
  private String datasetName;

  /** The dataflow id. */
  private Long dataflowId;

  /** The dataflow name. */
  private String dataflowName;

  /** The table schema id. */
  private String tableSchemaId;

  /** The table schema name. */
  private String tableSchemaName;

  /** The file name. */
  private String fileName;

  /** The error. */
  private String error;

  /** The dataset type. */
  private DatasetTypeEnum datasetType;

  /** The dataset schema id. */
  private String datasetSchemaId;

  /** The short code. */
  private String shortCode;

  /** The provider id. */
  private Long providerId;

  /** The invalid rules. */
  private Integer invalidRules;

  /** The disabled rules. */
  private Integer disabledRules;

  /** The dataset status. */
  private DatasetStatusEnum datasetStatus;

  /** The field schema id. */
  private String fieldSchemaId;

  /** The field schema name. */
  private String fieldSchemaName;
}
