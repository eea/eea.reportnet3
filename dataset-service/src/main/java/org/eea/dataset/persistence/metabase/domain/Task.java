package org.eea.dataset.persistence.metabase.domain;

import lombok.*;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;

import javax.persistence.*;
import java.util.Date;
import org.eea.interfaces.vo.metabase.TaskType;


/**
 * The Class Task.
 */
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "task")
public class Task {

  /** The id. */
  @Id
  @SequenceGenerator(name = "task_sequence_generator", sequenceName = "task_id_seq",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "task_sequence_generator")
  @Column(name = "id", columnDefinition = "serial")
  private Long id;

  /** The process id. */
  @Column(name = "process_id")
  private String processId;

  /** The status. */
  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private ProcessStatusEnum status;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private TaskType taskType;

  /** The create date. */
  @Column(name = "create_date")
  private Date createDate;

  /** The starting date. */
  @Column(name = "date_start")
  private Date startingDate;

  /** The finish date. */
  @Column(name = "date_finish")
  private Date finishDate;

  /** The json. */
  @Column(name = "json")
  private String json;

  /** The version. */
  @Version
  private int version;

  /** The pod. */
  @Column(name = "pod")
  private String pod;

  public Task(Long id, String processId, ProcessStatusEnum status, TaskType taskType, Date createDate, Date startingDate, Date finishDate, String json, int version) {
    this.id = id;
    this.processId = processId;
    this.status = status;
    this.taskType = taskType;
    this.createDate = createDate;
    this.startingDate = startingDate;
    this.finishDate = finishDate;
    this.json = json;
    this.version = version;
  }
}
