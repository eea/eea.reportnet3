package org.eea.interfaces.vo.dataflow;

import java.util.Date;
import org.eea.interfaces.vo.dataset.enums.MessageTypeEnum;
import lombok.Data;

/**
 * Instantiates a new message VO.
 */
@Data
public class MessageVO {

  /** The id. */
  private Long id;

  /** The provider id. */
  private Long providerId;

  /** The content. */
  private String content;

  /** The date. */
  private Date date;

  /** The read. */
  private boolean read;

  /** The direction. */
  private boolean direction;

  /** The type. */
  private MessageTypeEnum type;

  /** The message attachment VO. */
  private MessageAttachmentVO messageAttachmentVO;
}
