package org.eea.interfaces.vo.communication;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class UserNotificationListVO.
 */
@Getter
@Setter
@ToString
public class UserNotificationListVO {


  /** The user notifications. */
  private List<UserNotificationVO> userNotifications;

  /** The total records. */
  private Long totalRecords;

}
