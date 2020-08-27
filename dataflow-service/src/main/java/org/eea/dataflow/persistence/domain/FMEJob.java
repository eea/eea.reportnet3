package org.eea.dataflow.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.eea.interfaces.vo.dataflow.enums.FMEJobstatus;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "FME_JOBS")
public class FMEJob {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fme_jobs_id_seq")
  @SequenceGenerator(name = "fme_jobs_id_seq", allocationSize = 1)
  @Column(name = "ID")
  private Long id;

  @Column(name = "JOB_ID")
  private Long jobId;

  @Column(name = "DATASET_ID")
  private Long datasetId;

  @Column(name = "DATAFLOW_ID")
  private Long dataflowId;

  @Column(name = "PROVIDER_ID")
  private Long providerId;

  @Column(name = "FILE_NAME")
  private String fileName;

  @Column(name = "USER_NAME")
  private String userName;

  @Column(name = "OPERATION")
  @Enumerated(EnumType.STRING)
  private IntegrationOperationTypeEnum operation;

  @Column(name = "STATUS")
  @Enumerated(EnumType.STRING)
  private FMEJobstatus status;
}
