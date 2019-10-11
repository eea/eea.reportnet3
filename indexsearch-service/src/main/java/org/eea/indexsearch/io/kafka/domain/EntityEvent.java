package org.eea.indexsearch.io.kafka.domain;

import java.util.Objects;
import lombok.Data;

@Data
public class EntityEvent {

  private String entityType;
  private String eventType;
  private String entityURL;
  private String entityName;
  private String eventDescription;


}
