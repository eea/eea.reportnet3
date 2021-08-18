package org.eea.collaboration.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import lombok.Data;

/**
 * The Class MessageAttachment.
 */
@Data
@Entity
@Table(name = "message_attachment")
public class MessageAttachment {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_attachment_id_seq")
  @SequenceGenerator(name = "message_attachment_id_seq", sequenceName = "message_attachment_id_seq",
      allocationSize = 1)
  @Column(name = "id", columnDefinition = "serial")
  private Long id;

  /** The file name. */
  @Column(name = "file_name")
  private String fileName;

  /** The file size. */
  @Column(name = "file_size")
  private String fileSize;

  /** The content. */
  @Lob
  @Column(name = "content")
  @Type(type = "org.hibernate.type.BinaryType")
  private byte content[];

  /** The message. */
  @OneToOne
  @JoinColumn(name = "message_id")
  private Message message;
}
