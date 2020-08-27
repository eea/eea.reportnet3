package org.eea.dataflow.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import org.eea.interfaces.vo.dataflow.enums.FMEJobstatus;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class FMEJob.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "FME_JOBS")
public class FMEJob {

  /** The id job. */
  @Id
  @Column(name = "JOB_ID")
  private Long jobId;

  /** The name. */
  @Column(name = "DATASET_ID")
  private Long datasetId;

  /** The dataflow id. */
  @Column(name = "DATAFLOW_ID")
  private Long dataflowId;

  /** The provider id. */
  @Column(name = "PROVIDER_ID")
  private Long providerId;

  /** The description. */
  @Column(name = "R3USER")
  private String user;

  /** The file name. */
  @Column(name = "FILE_NAME")
  private String fileName;

  /** The status. */
  @Column(name = "OPERATION")
  @Enumerated(EnumType.STRING)
  private IntegrationOperationTypeEnum operation;

  /** The status. */
  @Column(name = "STATUS")
  @Enumerated(EnumType.STRING)
  private FMEJobstatus status;

}
