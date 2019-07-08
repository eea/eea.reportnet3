package org.eea.kafka.domain;

/**
 * The Enum EventType.
 */
public enum EventType {

  /** The connection created event. */
  /*
   * DATAFLOW_CREATED("ReportingTopic"), DATASET_CREATED("ReportingTopic"),
   * DATACOLLECTION_CREATED("ReportingTopic")
   */
  CONNECTION_CREATED_EVENT("Hello-Kafka2", "connection_key"),

  /** The hello kafka event. */
  HELLO_KAFKA_EVENT("Hello-Kafka2", "hello_kafka_key"),

  /** The validation finished event. */
  VALIDATION_FINISHED_EVENT("Hello-Kafka2", "validation_finished_key"),

  /** The load data completed event. */
  LOAD_DATA_COMPLETED_EVENT("Hello-Kafka2", "load_data_completed_key"),

  /** The load record completed event. */
  RECORD_UPDATED_COMPLETED_EVENT("Hello-Kafka2", "upload_record_completed_key"),

  /** The record created completed event. */
  RECORD_CREATED_COMPLETED_EVENT("Hello-Kafka2", "created_record_completed_key"),

  /** The load document completed event. */
  LOAD_DOCUMENT_COMPLETED_EVENT("Hello-Kafka2", "load_data_completed_key");

  /** The topic. */
  private String topic;


  /** The key. */
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
