package org.eea.interfaces.vo.communication;

import org.eea.interfaces.vo.communication.enums.NotificationLevelEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class SystemNotificationVO.
 */
@Getter
@Setter
@ToString
public class SystemNotificationVO {

  /** The id. */
  private String id;

  /** The message. */
  private String message;

  /** The enabled. */
  private boolean enabled;

  /** The level. */
  private NotificationLevelEnum level;

}
