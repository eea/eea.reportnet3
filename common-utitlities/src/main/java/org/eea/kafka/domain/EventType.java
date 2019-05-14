package org.eea.kafka.domain;

public enum EventType {
  /*DATAFLOW_CREATED("ReportingTopic"),
  DATASET_CREATED("ReportingTopic"),
  DATACOLLECTION_CREATED("ReportingTopic")*/
  CONNECTION_CREATED_EVENT("Hello-Kafka2","connection_key"),
  HELLO_KAFKA_EVENT("Hello-Kafka2","hello_kafka_key"),
  DATASET_PARSED_FILE_EVENT("ReportingTopic","ReportingTopic");

  private String topic;


  private String key;

  EventType(String topic, String key){
    this.topic=topic;
    this.key=key;
  }

  public String getTopic() {
    return this.topic;
  }

  public String getKey() {  return key;  }

}
