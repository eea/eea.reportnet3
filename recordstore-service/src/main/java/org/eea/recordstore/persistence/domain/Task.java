package org.eea.recordstore.persistence.domain;

import lombok.*;
import org.eea.interfaces.vo.metabase.TaskType;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;

import javax.persistence.*;
import java.util.Date;


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

  @Column(name = "task_type")
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
}
