package org.eea.kafka.domain;

import org.eea.utils.LiteralConstants;

/** The Enum EventType. */
public enum EventType {

  /** The connection created event. */
  CONNECTION_CREATED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "connection_key", true),

  /** The spread data event. */
  SPREAD_DATA_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "connection_key", true),

  /** The validation finished event. */
  VALIDATION_FINISHED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "validation_finished_key", true),

  /** The import reporting completed event. */
  IMPORT_REPORTING_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "import_reporting_completed_event", true),

  /** The import reporting failed event. */
  IMPORT_REPORTING_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "import_reporting_failed_event", true),

  /** The import design completed event. */
  IMPORT_DESIGN_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "import_design_completed_event", true),

  /** The import design failed event. */
  IMPORT_DESIGN_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "import_design_failed_event",
      true),

  /** The external import reporting completed event. */
  EXTERNAL_IMPORT_REPORTING_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_import_reporting_completed_event", true),

  /** The external import design completed event. */
  EXTERNAL_IMPORT_DESIGN_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_import_design_completed_event", true),

  /** The external export reporting completed event. */
  EXTERNAL_EXPORT_REPORTING_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_export_reporting_completed_event", true),

  /** The external export design completed event. */
  EXTERNAL_EXPORT_DESIGN_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_export_design_completed_event", true),

  /** The load record completed event. */
  RECORD_UPDATED_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "upload_record_completed_key", true),

  /** The record created completed event. */
  RECORD_CREATED_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "created_record_completed_key", true),

  /** The record created completed event. */
  RECORD_DELETED_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "created_record_completed_key", true),

  /** The delete table completed event. */
  DELETE_TABLE_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "delete_table_completed_event", true),

  /** The delete dataset data completed event. */
  DELETE_DATASET_DATA_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "delete_dataset_data_completed_event", true),

  /** The delete table schema completed event. */
  DELETE_TABLE_SCHEMA_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "delete_table_schema_completed_event", true),

  /** The field updated completed event. */
  FIELD_UPDATED_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "updated_field_completed_key", true),

  /** The snapshot restored event. */
  RESTORE_DATASET_SNAPSHOT_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "dataset_snapshot_restored_completed_key", true),

  /** The restore dataset snapshot failed event. */
  RESTORE_DATASET_SNAPSHOT_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "dataset_snapshot_restored_failed_key", true),

  /** The restore dataset schema snapshot completed event. */
  RESTORE_DATASET_SCHEMA_SNAPSHOT_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "dataset_schema_snapshot_restored_completed_key", true),

  /** The restore dataset schema snapshot failed event. */
  RESTORE_DATASET_SCHEMA_SNAPSHOT_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "dataset_schema_snapshot_restored_failed_key", true),

  /** The add dataset snapshot completed event. */
  ADD_DATASET_SNAPSHOT_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "add_dataset_snapshot_completed_event", true),

  /** The add datacollection snapshot completed event. */
  ADD_DATACOLLECTION_SNAPSHOT_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "add_dataCollection_snapshot_completed_event", true),

  /** The add dataset snapshot failed event. */
  ADD_DATASET_SNAPSHOT_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "add_dataset_snapshot_failed_event", true),

  /** The add dataset schema snapshot completed event. */
  ADD_DATASET_SCHEMA_SNAPSHOT_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "add_dataset_schema_snapshot_completed_event", true),

  /** The add dataset schema snapshot failed event. */
  ADD_DATASET_SCHEMA_SNAPSHOT_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "add_dataset_schema_snapshot_failed_event", true),

  /** The command execute validation. */
  COMMAND_EXECUTE_VALIDATION(LiteralConstants.COMMAND_TOPIC, "execute_validations_key", false),

  /** The command validate dataset. */
  COMMAND_VALIDATE_DATASET(LiteralConstants.COMMAND_TOPIC, "execute_dataset_validations_key",
      false),

  /** The command validate table. */
  COMMAND_VALIDATE_TABLE(LiteralConstants.COMMAND_TOPIC, "execute_table_validations_key", false),

  /** The command validate record. */
  COMMAND_VALIDATE_RECORD(LiteralConstants.COMMAND_TOPIC, "execute_record_validations_key", false),

  /** The command validate field. */
  COMMAND_VALIDATE_FIELD(LiteralConstants.COMMAND_TOPIC, "execute_field_validations_key", false),

  /** Command clean kyebase event type. */
  COMMAND_CLEAN_KYEBASE(LiteralConstants.BROADCAST_TOPIC, "command_clean_kyebase", true),

  /** The command validated dataset completed. */
  COMMAND_VALIDATED_DATASET_COMPLETED(LiteralConstants.BROADCAST_TOPIC, "dataset_validated_key",
      true),

  /** The command validated table completed. */
  COMMAND_VALIDATED_TABLE_COMPLETED(LiteralConstants.BROADCAST_TOPIC, "table_validated_key", true),

  /** The command validated record completed. */
  COMMAND_VALIDATED_RECORD_COMPLETED(LiteralConstants.BROADCAST_TOPIC, "record_validated_key",
      true),

  /** The command validated field completed. */
  COMMAND_VALIDATED_FIELD_COMPLETED(LiteralConstants.BROADCAST_TOPIC, "field_validated_key", true),

  /** The WebSocket notification event. */
  WEBSOCKET_NOTIFICATION(LiteralConstants.DATA_REPORTING_TOPIC, "websocket_notification", true),

  /** The lock method. */
  LOCK_METHOD(LiteralConstants.DATA_REPORTING_TOPIC, "lock_method", true),

  /** The lock entity. */
  LOCK_ENTITY(LiteralConstants.DATA_REPORTING_TOPIC, "lock_entity", true),

  /** The unlock method. */
  UNLOCK_METHOD(LiteralConstants.DATA_REPORTING_TOPIC, "unlock_method", true),

  /** The unlock entity. */
  UNLOCK_ENTITY(LiteralConstants.DATA_REPORTING_TOPIC, "unlock_entity", true),

  /** The upload document completed event. */
  UPLOAD_DOCUMENT_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "upload_document_completed_event", true),

  /** The upload document failed event. */
  UPLOAD_DOCUMENT_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "upload_document_failed_event", true),

  /** The delete document completed event. */
  DELETE_DOCUMENT_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "delete_document_completed_event", true),

  /** The delete document failed event. */
  DELETE_DOCUMENT_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "delete_document_failed_event", true),

  /** The command execute new design field propagation. */
  COMMAND_EXECUTE_NEW_DESIGN_FIELD_PROPAGATION(LiteralConstants.COMMAND_TOPIC,
      "execute_new_field_propagation", false),

  /** The release dataset snapshot completed event. */
  RELEASE_DATASET_SNAPSHOT_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "dataset_snapshot_released_completed_key", true),

  /** The release dataset snapshot failed event. */
  RELEASE_DATASET_SNAPSHOT_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "dataset_snapshot_released_failed_key", true),

  /** The command new design field propagation. */
  COMMAND_NEW_DESIGN_FIELD_PROPAGATION(LiteralConstants.COMMAND_TOPIC, "new_field_propagation",
      false),

  /** The add datacollection completed event. */
  ADD_DATACOLLECTION_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "add_datacollection_completed_event", true),

  /** The add datacollection failed event. */
  ADD_DATACOLLECTION_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "add_datacollection_failed_event", true),

  /** The update datacollection completed event. */
  UPDATE_DATACOLLECTION_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "update_datacollection_completed_event", true),

  /** The update datacollection failed event. */
  UPDATE_DATACOLLECTION_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "update_datacollection_failed_event", true),

  /** The release blocked event. */
  RELEASE_BLOCKED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "release_blocked_event", true),

  /** The updated document completed event. */
  UPDATED_DOCUMENT_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "update_document_completed_event", true),

  /** The invalidated qc rule event. */
  INVALIDATED_QC_RULE_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "invalidated_qc_rule_event",
      true),

  /** The validated qc rule event. */
  VALIDATED_QC_RULE_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "validated_qc_rule_event", true),

  /** The copy dataset schema completed event. */
  COPY_DATASET_SCHEMA_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "copy_dataset_schema_completed_event", true),

  /** The copy dataset schema failed event. */
  COPY_DATASET_SCHEMA_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "copy_dataset_schema_completed_event", true),

  /** The copy dataset schema not found event. */
  COPY_DATASET_SCHEMA_NOT_FOUND_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "copy_dataset_schema_not_found_event", true);



  /** The topic. */
  private String topic;

  /** The key. */
  private String key;

  /** The sorted. */
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
