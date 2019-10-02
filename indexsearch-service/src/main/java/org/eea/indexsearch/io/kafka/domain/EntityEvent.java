package org.eea.indexsearch.io.kafka.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class EntityEvent {
  private String entityType;
  private String eventType;
  private String entityURL;
  private String entityName;
  private String eventDescription;

}
