package org.eea.interfaces.vo.dataflow;

import java.util.List;
import lombok.Data;

/**
 * The Class MessagePaginatedVO.
 */
@Data
public class MessagePaginatedVO {

  /** The list message VO. */
  List<MessageVO> listMessage;

  /** The total messages. */
  private Long totalMessages;
}
