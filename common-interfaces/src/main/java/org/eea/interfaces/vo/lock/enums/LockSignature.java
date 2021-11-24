package org.eea.interfaces.vo.lock.enums;

/** The Enum LockSignature. */
public enum LockSignature {

  /** The empty. */
  EMPTY(""),

  /** DatasetControllerImpl.importFileData(..) */
  IMPORT_FILE_DATA("DatasetControllerImpl.importFileData(..)"),

  /** DatasetSnapshotControllerImpl.createSnapshot(..) */
  CREATE_SNAPSHOT("DatasetSnapshotControllerImpl.createSnapshot(..)"),

  /** DatasetSnapshotControllerImpl.restoreSnapshot(..) */
  RESTORE_SNAPSHOT("DatasetSnapshotControllerImpl.restoreSnapshot(..)"),

  /** ValidationHelper.executeValidation(..) */
  EXECUTE_VALIDATION("ValidationHelper.executeValidation(..)"),

  /** ValidationControllerImpl.validateDataSetData(..) */
  FORCE_EXECUTE_VALIDATION("ValidationControllerImpl.validateDataSetData(..)"),

  /** DatasetSnapshotControllerImpl.createSchemaSnapshot(..) */
  CREATE_SCHEMA_SNAPSHOT("DatasetSnapshotControllerImpl.createSchemaSnapshot(..)"),

  /** DatasetSnapshotControllerImpl.restoreSchemaSnapshot(..) */
  RESTORE_SCHEMA_SNAPSHOT("DatasetSnapshotControllerImpl.restoreSchemaSnapshot(..)"),

  /** DatasetSnapshotControllerImpl.createReleaseSnapshots(..) */
  RELEASE_SNAPSHOTS("DatasetSnapshotControllerImpl.createReleaseSnapshots(..)"),

  /** DataCollectionControllerImpl.createEmptyDataCollection(..) */
  CREATE_DATA_COLLECTION("DataCollectionControllerImpl.createEmptyDataCollection(..)"),

  /** DatasetControllerImpl.deleteImportTable(..) */
  DELETE_IMPORT_TABLE("DatasetControllerImpl.deleteTableData(..)"),

  /** DatasetControllerImpl.deleteImportData(..) */
  DELETE_DATASET_VALUES("DatasetControllerImpl.deleteDatasetData(..)"),

  /** DataCollectionControllerImpl.updateDataCollection(..) */
  UPDATE_DATA_COLLECTION("DataCollectionControllerImpl.updateDataCollection(..)"),

  /** DatasetSchemaControllerImpl.copyDesignsFromDataflow(..) */
  COPY_DATASET_SCHEMA("DatasetSchemaControllerImpl.copyDesignsFromDataflow(..)"),

  /** EUDatasetControllerImpl.populateDataFromDataCollection(..) */
  POPULATE_EU_DATASET("EUDatasetControllerImpl.populateDataFromDataCollection(..)"),

  /** IntegrationControllerImpl.executeEUDatasetExport(..) */
  EXPORT_EU_DATASET("IntegrationControllerImpl.executeEUDatasetExport(..)"),

  /** DatasetControllerImpl.deleteRecord(..) */
  DELETE_RECORDS("DatasetControllerImpl.deleteRecord(..)"),

  /** DatasetControllerImpl.insertRecords(..) */
  INSERT_RECORDS("DatasetControllerImpl.insertRecords(..)"),

  /** DatasetControllerImpl.insertRecordsMultiTable(..) */
  INSERT_RECORDS_MULTITABLE("DatasetControllerImpl.insertRecordsMultiTable(..)"),

  /** DatasetControllerImpl.updateRecords(..) */
  UPDATE_RECORDS("DatasetControllerImpl.updateRecords(..)"),

  /** DatasetControllerImpl.updateField(..) */
  UPDATE_FIELD("DatasetControllerImpl.updateField(..)"),

  /** DatasetSchemaControllerImpl.deleteFieldSchema(..) */
  DELETE_FIELD_SCHEMA("DatasetSchemaControllerImpl.deleteFieldSchema(..)"),

  /** IntegrationControllerImpl.executeExternalIntegration(..) */
  EXECUTE_EXTERNAL_INTEGRATION("IntegrationControllerImpl.executeExternalIntegration(..)"),

  /** DatasetSchemaControllerImpl.importSchemas(..) */
  IMPORT_SCHEMAS("DatasetSchemaControllerImpl.importSchemas(..)");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new lock type.
   *
   * @param value the value
   */
  LockSignature(String value) {
    this.value = value;
  }

  /**
   * Gets the value.
   *
   * @return the value
   */
  public String getValue() {
    return value;
  }
}
