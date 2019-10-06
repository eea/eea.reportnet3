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
  CONNECTION_CREATED_EVENT("DATA_REPORTING_TOPIC", "connection_key", true),

  /**
   * The hello kafka event.
   */
  HELLO_KAFKA_EVENT("DATA_REPORTING_TOPIC", "hello_kafka_key", true),

  /**
   * The validation finished event.
   */
  VALIDATION_FINISHED_EVENT("DATA_REPORTING_TOPIC", "validation_finished_key", true),

  /**
   * The load data completed event.
   */
  LOAD_DATA_COMPLETED_EVENT("DATA_REPORTING_TOPIC", "load_data_completed_key", true),

  /**
   * The load record completed event.
   */
  RECORD_UPDATED_COMPLETED_EVENT("DATA_REPORTING_TOPIC", "upload_record_completed_key", true),

  /**
   * The record created completed event.
   */
  RECORD_CREATED_COMPLETED_EVENT("DATA_REPORTING_TOPIC", "created_record_completed_key", true),

  /**
   * The record created completed event.
   */
  RECORD_DELETED_COMPLETED_EVENT("DATA_REPORTING_TOPIC", "created_record_completed_key", true),

  /**
   * The deleted table.
   */
  DELETED_TABLE("DATA_REPORTING_TOPIC", "deleted_table", true),

  /**
   * The load document completed event.
   */
  LOAD_DOCUMENT_COMPLETED_EVENT("DATA_REPORTING_TOPIC", "load_document_completed_key", true),

  /**
   * The delete document completed event.
   */
  DELETE_DOCUMENT_COMPLETED_EVENT("DATA_REPORTING_TOPIC", "delete_document_completed_key", true),

  /**
   * The field updated completed event.
   */
  FIELD_UPDATED_COMPLETED_EVENT("DATA_REPORTING_TOPIC", "updated_field_completed_key", true),

  /**
   * The snapshot restored event.
   */
  SNAPSHOT_RESTORED_EVENT("DATA_REPORTING_TOPIC", "snapshot_restored_completed_key", true),

  /**
   * The command execute validation.
   */
  COMMAND_EXECUTE_VALIDATION("COMMAND_TOPIC", "execute_validations_key", false),

  /**
   * The command validate dataset.
   */
  COMMAND_VALIDATE_DATASET("COMMAND_TOPIC", "execute_dataset_validations_key", false),

  /**
   * The command validate table.
   */
  COMMAND_VALIDATE_TABLE("COMMAND_TOPIC", "execute_table_validations_key", false),

  /**
   * The command validate record.
   */
  COMMAND_VALIDATE_RECORD("COMMAND_TOPIC", "execute_record_validations_key", false),

  /**
   * The command validate field.
   */
  COMMAND_VALIDATE_FIELD("COMMAND_TOPIC", "execute_field_validations_key", false),

  /**
   * The command validated dataset completed.
   */
  COMMAND_VALIDATED_DATASET_COMPLETED("BROADCAST_TOPIC", "dataset_validated_key", true),

  /**
   * The command validated table completed.
   */
  COMMAND_VALIDATED_TABLE_COMPLETED("BROADCAST_TOPIC", "table_validated_key", true),

  /**
   * The command validated record completed.
   */
  COMMAND_VALIDATED_RECORD_COMPLETED("BROADCAST_TOPIC", "record_validated_key", true),

  /**
   * The command validated field completed.
   */
  COMMAND_VALIDATED_FIELD_COMPLETED("BROADCAST_TOPIC", "field_validated_key", true),

  /**
   * The WebSocket notification event.
   */
  WEBSOCKET_NOTIFICATION("DATA_REPORTING_TOPIC", "websocket_notification", true);


  /**
   * The topic.
   */
  private String topic;


  /**
   * The key.
   */
  private String key;

  /**
   * The sorted.
   */
  private Boolean sorted;

  /**
   * Instantiates a new event type.
   *
   * @param topic the topic
   * @param key the key
   * @param sorted the sorted
   */
  EventType(String topic, String key, Boolean sorted) {
    this.topic = topic;
    this.key = key;
    this.sorted = sorted;
  }

  /**
   * Gets the sorted.
   *
   * @return the sorted
   */
  public Boolean isSorted() {
    return sorted;
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
