package org.eea.indexsearch.io.kafka.domain;

import lombok.Data;

/**
 * Instantiates a new entity event.
 */
@Data
public class EntityEvent {

  /** The entity type. */
  private String entityType;

  /** The event type. */
  private String eventType;

  /** The entity URL. */
  private String entityURL;

  /** The entity name. */
  private String entityName;

  /** The event description. */
  private String eventDescription;


}
