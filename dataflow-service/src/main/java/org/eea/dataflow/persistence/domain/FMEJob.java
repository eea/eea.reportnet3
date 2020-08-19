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

@Entity
@Getter
@Setter
@ToString
@Table(name = "FME_JOBS")
public class FMEJob {

  @Id
  @Column(name = "IDJOB")
  private Long idJob;

  /** The name. */
  @Column(name = "DATASET_ID")
  private Long datasetId;

  /** The description. */
  @Column(name = "R3USER")
  private String user;

  /** The status. */
  @Column(name = "OPERATION")
  @Enumerated(EnumType.STRING)
  private IntegrationOperationTypeEnum operation;

  /** The status. */
  @Column(name = "STATUS")
  @Enumerated(EnumType.STRING)
  private FMEJobstatus status;

}
