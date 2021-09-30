package org.eea.collaboration.persistence.domain;

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
import org.eea.interfaces.vo.dataset.enums.MessageTypeEnum;
import lombok.Data;

/**
 * Instantiates a new message.
 */
@Data
@Entity
@Table(name = "message")
public class Message {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_id_seq")
  @SequenceGenerator(name = "message_id_seq", sequenceName = "message_id_seq", allocationSize = 1)
  @Column(name = "id", columnDefinition = "serial")
  private Long id;

  /** The dataflow id. */
  @Column(name = "dataflow_id")
  private Long dataflowId;

  /** The provider id. */
  @Column(name = "provider_id")
  private Long providerId;

  /** The content. */
  @Column(name = "content")
  private String content;

  /** The date. */
  @Column(name = "date")
  private Date date;

  /** The direction. */
  @Column(name = "direction")
  private boolean direction;

  /** The read. */
  @Column(name = "read")
  private boolean read;

  /** The user name. */
  @Column(name = "user_name")
  private String userName;

  /** The type. */
  @Column(name = "TYPE")
  @Enumerated(EnumType.STRING)
  private MessageTypeEnum type;

  /** The automatic. */
  @Column(name = "automatic")
  private boolean automatic;

  /** The file size. */
  @Column(name = "file_size")
  private String fileSize;
}
