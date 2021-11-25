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

  /** The validation release finished event. */
  VALIDATION_RELEASE_FINISHED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "validation_release_finished_key", true),

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

  /** The external import reporting from other system completed event. */
  EXTERNAL_IMPORT_REPORTING_FROM_OTHER_SYSTEM_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_import_reporting_from_other_system_completed_event", true),

  /** The external import design from other system completed event. */
  EXTERNAL_IMPORT_DESIGN_FROM_OTHER_SYSTEM_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_import_design_from_other_system_completed_event", true),


  /** The external import reporting from other system failed event. */
  EXTERNAL_IMPORT_REPORTING_FROM_OTHER_SYSTEM_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_import_reporting_from_other_system_failed_event", true),


  /** The external import design from other system failed event. */
  EXTERNAL_IMPORT_DESIGN_FROM_OTHER_SYSTEM_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_import_design_from_other_system_failed_event", true),

  /** The external import reporting completed event. */
  EXTERNAL_IMPORT_REPORTING_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_import_reporting_completed_event", true),

  /** The external import reporting failed event. */
  EXTERNAL_IMPORT_REPORTING_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_import_reporting_failed_event", true),

  /** The external import design completed event. */
  EXTERNAL_IMPORT_DESIGN_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_import_design_completed_event", true),

  /** The external import design failed event. */
  EXTERNAL_IMPORT_DESIGN_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_import_design_failed_event", true),

  /** The external export reporting completed event. */
  EXTERNAL_EXPORT_REPORTING_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_export_reporting_completed_event", true),

  /** The external export reporting failed event. */
  EXTERNAL_EXPORT_REPORTING_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_export_reporting_failed_event", true),

  /** The external export design completed event. */
  EXTERNAL_EXPORT_DESIGN_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_export_design_completed_event", true),

  /** The external export design failed event. */
  EXTERNAL_EXPORT_DESIGN_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_export_design_failed_event", true),

  /** The external export eudataset completed event. */
  EXTERNAL_EXPORT_EUDATASET_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_export_eudataset_completed_event", true),

  /** The external export eudataset failed event. */
  EXTERNAL_EXPORT_EUDATASET_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "external_export_eudataset_failed_event", true),

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

  /** The delete dataset schema completed event. */
  DELETE_DATASET_SCHEMA_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "delete_dataset_schema_completed_event", true),

  /** The delete table schema completed event. */
  DELETE_TABLE_SCHEMA_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "delete_table_schema_completed_event", true),

  /** The field updated completed event. */
  FIELD_UPDATED_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "updated_field_completed_key", true),

  /** The snapshot restored event. */
  RESTORE_DATASET_SNAPSHOT_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "dataset_snapshot_restored_completed_key", true),

  /** The restore datacollection snapshot completed event. */
  RESTORE_DATACOLLECTION_SNAPSHOT_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "dataCollection_snapshot_restored_completed_key", true),

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

  /** The copy data to eudataset completed event. */
  COPY_DATA_TO_EUDATASET_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "copy_data_to_eudataset_completed_event", true),

  /** The copy data to eudataset failed event. */
  COPY_DATA_TO_EUDATASET_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "copy_data_to_eudataset_failed_event", true),

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

  /** The delete dataflow completed event. */
  DELETE_DATAFLOW_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "delete_dataflow_completed_event", true),

  /** The delete dataflow failed event. */
  DELETE_DATAFLOW_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "delete_dataflow_failed_event", true),

  /** The command execute new design field propagation. */
  COMMAND_EXECUTE_NEW_DESIGN_FIELD_PROPAGATION(LiteralConstants.COMMAND_TOPIC,
      "execute_new_field_propagation", false),


  /** The release snapshot completed event. */
  RELEASE_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "dataset_released_completed_key",
      true),

  /** The release provider completed event. */
  RELEASE_PROVIDER_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "dataset_provider_released_completed_key", true),

  /** The release snapshot onebyone completed event. */
  RELEASE_ONEBYONE_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "dataset_onebyone_released_completed_key", true),

  /** The release snapshot failed event. */
  RELEASE_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "dataset_released_failed_key", true),

  /** The release snapshot blocked failed event. */
  RELEASE_BLOCKERS_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "dataset_released_blockers_failed_key", true),

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

  /** The validate rules finish event. */
  VALIDATE_RULES_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "validate_rules_completed_event", true),

  /** The copy dataset schema completed event. */
  COPY_DATASET_SCHEMA_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "copy_dataset_schema_completed_event", true),

  /** The copy dataset schema failed event. */
  COPY_DATASET_SCHEMA_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "copy_dataset_schema_completed_event", true),

  /** The copy dataset schema not found event. */
  COPY_DATASET_SCHEMA_NOT_FOUND_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "copy_dataset_schema_not_found_event", true),

  /** The data delete to replace completed event. */
  DATA_DELETE_TO_REPLACE_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "data_delete_to_replace_completed_event", true),

  /** The command create query views event. */
  CREATED_QUERY_VIEWS_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "created_query_views_event",
      true),

  /** The validate manual qc event. */
  VALIDATE_MANUAL_QC_COMMAND(LiteralConstants.DATA_REPORTING_TOPIC, "validate_manual_qc_command",
      true),

  /** The disable sql rules event. */
  DISABLE_RULES_ERROR_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "disable_rules_error_event",
      true),

  /** The validate rules error event. */
  VALIDATE_RULES_ERROR_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "validate_rules_error_event",
      true),

  /** The disable rules error event. */
  DISABLE_NAMES_TYPES_RULES_ERROR_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "disable_names_types_rules_error_event", true),

  /** The create update rule event. */
  CREATE_UPDATE_RULE_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "create_update_rule_event", true),

  /** The received message. */
  RECEIVED_MESSAGE(LiteralConstants.DATA_REPORTING_TOPIC, "received_message", true),

  /** The updated dataset status. */
  UPDATED_DATASET_STATUS(LiteralConstants.DATA_REPORTING_TOPIC, "updated_dataset_status", true),

  /** The update materialized view event. */
  UPDATE_MATERIALIZED_VIEW_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "update_materialized_view_event", true),

  /** The refresh materialized view event. */
  REFRESH_MATERIALIZED_VIEW_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "refresh_materialized_view_event", true),

  /** The create update view event. */
  CREATE_UPDATE_VIEW_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "create_update_view_event", true),

  /** The delete view procces event. */
  DELETE_VIEW_PROCCES_EVENT(LiteralConstants.BROADCAST_TOPIC, "delete_view_procces_event", true),

  /** The finish view procces event. */
  FINISH_VIEW_PROCCES_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "finish_view_procces_event",
      true),

  /** The insert view procces event. */
  INSERT_VIEW_PROCCES_EVENT(LiteralConstants.BROADCAST_TOPIC, "insert_view_procces_event", true),

  /** The datacollection national coordinator event. */
  DATACOLLECTION_NATIONAL_COORDINATOR_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "datacollection_national_coordinator_event", true),


  /** The import dataset schema completed event. */
  IMPORT_DATASET_SCHEMA_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "import_dataset_schema_completed_event", true),


  /** The import dataset schema failed event. */
  IMPORT_DATASET_SCHEMA_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "import_dataset_schema_completed_event", true),


  /** The sort field desing failed event. */
  SORT_FIELD_DESIGN_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "sort_field_design_failed_event", true),

  /** The sort field failed event. */
  SORT_FIELD_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "sort_field_failed_event", true),

  /** The export dataset completed event. */
  EXPORT_DATASET_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "export_dataset_completed_event", true),

  /** The download dataset validations completed event. */
  DOWNLOAD_VALIDATIONS_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "download_validations_completed_event", true),

  /** The download dataset validations failed event. */
  DOWNLOAD_VALIDATIONS_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "download_validations_failed_event", true),

  /** The export qc completed event. */
  EXPORT_QC_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "export_qc_completed_event",
      true),

  /** The export qc failed event. */
  EXPORT_QC_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "export_qc_failed_event", true),

  /** The validate reporters completed event. */
  VALIDATE_REPORTERS_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "validate_reporters_completed_event", true),

  /** The validate reporters failed event. */
  VALIDATE_REPORTERS_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "validate_reporters_failed_event", true),

  /** The validate reporters completed event. */
  VALIDATE_LEAD_REPORTERS_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "validate_lead_reporters_completed_event", true),

  /** The validate reporters failed event. */
  VALIDATE_LEAD_REPORTERS_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "validate_lead_reporters_failed_event", true),

  /** The validate all reporters completed event. */
  VALIDATE_ALL_REPORTERS_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "validate_all_reporters_completed_event", true),

  /** The validate all reporters failed event. */
  VALIDATE_ALL_REPORTERS_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "validate_all_reporters_failed_event", true),

  /** The export dataset failed event. */
  EXPORT_DATASET_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "export_dataset_failed_event",
      true),

  /** The reference dataflow processed event. */
  REFERENCE_DATAFLOW_PROCESSED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "reference_dataflow_processed_event", true),

  /** The reference dataflow process failed event. */
  REFERENCE_DATAFLOW_PROCESS_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "reference_dataflow_process_failed_event", true),

  /** The import field schema completed event. */
  IMPORT_FIELD_SCHEMA_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "import_field_schema_completed_event", true),

  /** The import field schema failed event. */
  IMPORT_FIELD_SCHEMA_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "import_field_schema_failed_event", true),

  /** The no pk reference dataflow error event. */
  NO_PK_REFERENCE_DATAFLOW_ERROR_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "no_pk_reference_dataflow_error_event", true),

  /** The export schema information completed event. */
  EXPORT_SCHEMA_INFORMATION_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "export_schema_information_completed_event", true),

  /** The export schema information failed event. */
  EXPORT_SCHEMA_INFORMATION_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "export_schema_information_failed_event", true),

  /** The no enabled system notifications. */
  NO_ENABLED_SYSTEM_NOTIFICATIONS(LiteralConstants.DATA_REPORTING_TOPIC,
      "no_enabled_system_notifications", true),

  /** The export users by country completed event. */
  EXPORT_USERS_BY_COUNTRY_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "export_users_by_country_completed_event", true),

  /** The export users by country failed event. */
  EXPORT_USERS_BY_COUNTRY_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "export_users_by_country_failed_event", true),

  /** The copy reference dataset snapshot completed event. */
  COPY_REFERENCE_DATASET_SNAPSHOT_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "copy_reference_dataset_snapshot_completed_event", true),

  /** The restore prefilling reference snapshot completed event. */
  RESTORE_PREFILLING_REFERENCE_SNAPSHOT_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "restore_prefilling_reference_snapshot_completed_event", true),

  /** The continue fme process event. */
  CONTINUE_FME_PROCESS_EVENT(LiteralConstants.DATA_REPORTING_TOPIC, "continue_fme_process_event",
      true),

  /** The export table data completed event. */
  EXPORT_TABLE_DATA_COMPLETED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "export_table_data_completed_event", true),

  /** The export table data failed event. */
  EXPORT_TABLE_DATA_FAILED_EVENT(LiteralConstants.DATA_REPORTING_TOPIC,
      "export_table_data_failed_event", true);

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
