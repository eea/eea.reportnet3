package org.eea.validation.persistence.data.metabase.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


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
