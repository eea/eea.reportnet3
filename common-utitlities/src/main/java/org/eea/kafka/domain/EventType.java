package org.eea.kafka.domain;

/**
 * The Enum EventType.
 */
public enum EventType {

  /**
   * The connection created event.
   */
  /*
   * DATAFLOW_CREATED("ReportingTopic"), DATASET_CREATED("ReportingTopic"),
   * DATACOLLECTION_CREATED("ReportingTopic")
   */
  CONNECTION_CREATED_EVENT("DATA_REPORTING_TOPIC", "connection_key"),

  /**
   * The hello kafka event.
   */
  HELLO_KAFKA_EVENT("DATA_REPORTING_TOPIC", "hello_kafka_key"),

  /**
   * The validation finished event.
   */
  VALIDATION_FINISHED_EVENT("DATA_REPORTING_TOPIC", "validation_finished_key"),

  /**
   * The load data completed event.
   */
  LOAD_DATA_COMPLETED_EVENT("DATA_REPORTING_TOPIC", "load_data_completed_key"),

  /**
   * The load record completed event.
   */
  RECORD_UPDATED_COMPLETED_EVENT("DATA_REPORTING_TOPIC", "upload_record_completed_key"),

  /**
   * The record created completed event.
   */
  RECORD_CREATED_COMPLETED_EVENT("DATA_REPORTING_TOPIC", "created_record_completed_key"),

  /**
   * The record created completed event.
   */
  RECORD_DELETED_COMPLETED_EVENT("DATA_REPORTING_TOPIC", "created_record_completed_key"),

  DELETED_TABLE("DATA_REPORTING_TOPIC", "deleted_table"),

  /**
   * The load document completed event.
   */
  LOAD_DOCUMENT_COMPLETED_EVENT("DATA_REPORTING_TOPIC", "load_document_completed_key"),

  /**
   * The delete document completed event.
   */
  DELETE_DOCUMENT_COMPLETED_EVENT("DATA_REPORTING_TOPIC", "delete_document_completed_key"),

  /**
   * The field updated completed event.
   */
  FIELD_UPDATED_COMPLETED_EVENT("DATA_REPORTING_TOPIC", "updated_field_completed_key"),

  /**
   * The snapshot restored event.
   */
  SNAPSHOT_RESTORED_EVENT("DATA_REPORTING_TOPIC", "snapshot_restored_completed_key"),

  /**
   * The WebSocket notification event.
   */
  WEBSOCKET_NOTIFICATION("DATA_REPORTING_TOPIC", "websocket_notification");


  /**
   * The topic.
   */
  private String topic;


  /**
   * The key.
   */
  private String key;

  /**
   * Instantiates a new event type.
   *
   * @param topic the topic
   * @param key the key
   */
  EventType(String topic, String key) {
    this.topic = topic;
    this.key = key;
  }

  /**
   * Gets the topic.
   *
   * @return the topic
   */
  public String getTopic() {
    return this.topic;
  }

  /**
   * Gets the key.
   *
   * @return the key
   */
  public String getKey() {
    return key;
  }

}
