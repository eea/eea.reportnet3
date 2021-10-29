package org.eea.interfaces.vo.communication;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class UserNotificationVO.
 */
@Getter
@Setter
@ToString
public class UserNotificationVO {

  /** The event type. */
  private String eventType;

  /** The insert date. */
  private Date insertDate;

  /** The content. */
  private UserNotificationContentVO content;
}
