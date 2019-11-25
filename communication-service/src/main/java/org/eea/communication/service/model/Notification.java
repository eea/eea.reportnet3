package org.eea.communication.service.model;

import java.util.Map;

/**
 * The type Notification.
 */
public class Notification {
  /**
   * The Class Notification.
   */

  /**
   * The content.
   */
  private Map<?, ?> content;

  /**
   * Instantiates a new notification.
   * 
   * @param message the message
   */
  public Notification(Map<?, ?> content) {
    this.content = content;
  }

  /**
   * Gets the content.
   * 
   * @return the content
   */
  public Map<?, ?> getContent() {
    return content;
  }

}
