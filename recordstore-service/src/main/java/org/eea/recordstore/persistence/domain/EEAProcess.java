package org.eea.recordstore.persistence.domain;

import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Entity
@Getter
@Setter
@ToString
@Table(name = "PROCESS")
public class EEAProcess {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "process_id_seq")
  @SequenceGenerator(name = "process_id_seq", sequenceName = "process_id_seq", initialValue = 1,
      allocationSize = 1)
  @Column(name = "id", columnDefinition = "serial")
  private Long id;

  /** The dataset id. */
  @Column(name = "dataset_id")
  private Long datasetId;

  /** The dataset name. */
  @Transient
  private String datasetName;

  /** The dataflow id. */
  @Column(name = "dataflow_id")
  private Long dataflowId;

  /** The dataflow name. */
  @Transient
  private String dataflowName;

  /** The process type. */
  @Column(name = "process_type")
  @Enumerated(EnumType.STRING)
  private ProcessTypeEnum processType;

  /** The user. */
  @Column(name = "username")
  private String user;

  /** The process id. */
  @Column(name = "process_id")
  private String processId;

  /** The status. */
  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private ProcessStatusEnum status;

  /** The queued date. */
  @Column(name = "queued_date")
  private Date queuedDate;

  /** The process starting date. */
  @Column(name = "date_start")
  private Date processStartingDate;

  /** The process finishing date. */
  @Column(name = "date_finish")
  private Date processFinishingDate;

  /** The priority. */
  @Column(name = "priority")
  private int priority;

  /** The released. */
  @Column(name = "released")
  private boolean released;

  /** The version. */
  @Version
  private int version;

  /**
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final EEAProcess dataflow = (EEAProcess) o;
    return id.equals(dataflow.id);

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, datasetId, dataflowId, processType, user, processId, status, queuedDate,
        processStartingDate, processFinishingDate);
  }

}
