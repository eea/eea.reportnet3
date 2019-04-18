package org.eea.kafka.domain;


import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class EEAEventVO implements Serializable {

  private static final long serialVersionUID = -3529245031747221245L;
  private EventType eventType;
  private Map<String,Object> data;

  public EventType getEventType() {
    return eventType;
  }

  public void setEventType(EventType eventType) {
    this.eventType = eventType;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public void setData(Map<String, Object> data) {
    this.data = data;
  }

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
    return Objects.hash(eventType);
  }

  @Override
  public String toString() {
    return "EEAEventVO{" +
        "eventType=" + eventType +
        '}';
  }
}
