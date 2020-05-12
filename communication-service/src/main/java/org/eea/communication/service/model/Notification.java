package org.eea.communication.service.model;

import java.util.Map;
import org.eea.kafka.domain.EventType;

/**
 * The type Notification.
 */
public class Notification {

  /** The Class Notification. */
  private EventType type;

  /** The content. */
  private Map<String, Object> content;

  /**
   * Instantiates a new notification.
   *
   * @param type the type
   * @param content the content
   */
  public Notification(EventType type, Map<String, Object> content) {
    this.type = type;
    this.content = content;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public EventType getType() {
    return type;
  }

  /**
   * Gets the content.
   * 
   * @return the content
   */
  public Map<String, Object> getContent() {
    return content;
  }
}
