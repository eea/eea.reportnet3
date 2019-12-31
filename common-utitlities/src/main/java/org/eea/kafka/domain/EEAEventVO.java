package org.eea.kafka.domain;


import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * The Class EEAEventVO.
 */
public class EEAEventVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -3529245031747221245L;

  /** The event type. */
  private EventType eventType;

  /** The data. */
  private Map<String, Object> data;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  public EventType getEventType() {
    return eventType;
  }

  /**
   * Sets the event type.
   *
   * @param eventType the new event type
   */
  public void setEventType(EventType eventType) {
    this.eventType = eventType;
  }

  /**
   * Gets the data.
   *
   * @return the data
   */
  public Map<String, Object> getData() {
    return data;
  }

  /**
   * Sets the data.
   *
   * @param data the data
   */
  public void setData(Map<String, Object> data) {
    this.data = data;
  }

  /**
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EEAEventVO that = (EEAEventVO) o;
    return eventType == that.eventType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, eventType.ordinal());
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "EEAEventVO{" + "eventType=" + eventType + ", data=" + data + '}';
  }
}
