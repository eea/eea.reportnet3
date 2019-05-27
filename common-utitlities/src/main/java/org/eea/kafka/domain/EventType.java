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

  /** The dataset parsed file event. */
  DATASET_PARSED_FILE_EVENT("ReportingTopic", "ReportingTopic");

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
