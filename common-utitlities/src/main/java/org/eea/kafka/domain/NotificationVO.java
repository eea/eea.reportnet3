package org.eea.kafka.domain;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
/**
 * The class that works as interface to fulfill notifiable events
 */
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
}
