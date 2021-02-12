package org.eea.interfaces.vo.lock.enums;

/** The Enum LockSignature. */
public enum LockSignature {

  /** The empty. */
  EMPTY(""),

  /** DataSetControllerImpl.importFileData(..) */
  IMPORT_FILE_DATA("DataSetControllerImpl.importFileData(..)"),

  /** DataSetSnapshotControllerImpl.createSnapshot(..) */
  CREATE_SNAPSHOT("DataSetSnapshotControllerImpl.createSnapshot(..)"),

  /** DataSetSnapshotControllerImpl.restoreSnapshot(..) */
  RESTORE_SNAPSHOT("DataSetSnapshotControllerImpl.restoreSnapshot(..)"),

  /** ValidationHelper.executeValidation(..) */
  EXECUTE_VALIDATION("ValidationHelper.executeValidation(..)"),

  /** ValidationControllerImpl.validateDataSetData(..) */
  FORCE_EXECUTE_VALIDATION("ValidationControllerImpl.validateDataSetData(..)"),

  /** DataSetSnapshotControllerImpl.createSchemaSnapshot(..) */
  CREATE_SCHEMA_SNAPSHOT("DataSetSnapshotControllerImpl.createSchemaSnapshot(..)"),

  /** DataSetSnapshotControllerImpl.restoreSchemaSnapshot(..) */
  RESTORE_SCHEMA_SNAPSHOT("DataSetSnapshotControllerImpl.restoreSchemaSnapshot(..)"),

  /** DataSetSnapshotControllerImpl.createReleaseSnapshots(..) */
  RELEASE_SNAPSHOTS("DataSetSnapshotControllerImpl.createReleaseSnapshots(..)"),

  /** DataSetSnapshotControllerImpl.releaseSnapshot(..) */
  RELEASE_SNAPSHOT("DataSetSnapshotControllerImpl.releaseSnapshot(..)"),

  /** DataCollectionControllerImpl.createEmptyDataCollection(..) */
  CREATE_DATA_COLLECTION("DataCollectionControllerImpl.createEmptyDataCollection(..)"),

  /** DataSetControllerImpl.deleteImportTable(..) */
  DELETE_IMPORT_TABLE("DataSetControllerImpl.deleteImportTable(..)"),

  /** DataSetControllerImpl.deleteImportData(..) */
  DELETE_DATASET_VALUES("DataSetControllerImpl.deleteImportData(..)"),

  /** DataCollectionControllerImpl.updateDataCollection(..) */
  UPDATE_DATA_COLLECTION("DataCollectionControllerImpl.updateDataCollection(..)"),

  /** DatasetSchemaControllerImpl.copyDesignsFromDataflow(..) */
  COPY_DATASET_SCHEMA("DatasetSchemaControllerImpl.copyDesignsFromDataflow(..)"),

  /** EUDatasetControllerImpl.populateDataFromDataCollection(..) */
  POPULATE_EU_DATASET("EUDatasetControllerImpl.populateDataFromDataCollection(..)"),

  /** IntegrationControllerImpl.executeEUDatasetExport(..) */
  EXPORT_EU_DATASET("IntegrationControllerImpl.executeEUDatasetExport(..)"),

  /** The delete records. */
  DELETE_RECORDS("DataSetControllerImpl.deleteRecord(..)"),

  /** The insert records. */
  INSERT_RECORDS("DataSetControllerImpl.insertRecords(..)"),

  /** The insert records multitable. */
  INSERT_RECORDS_MULTITABLE("DataSetControllerImpl.insertRecordsMultiTable(..)"),

  /** The update records. */
  UPDATE_RECORDS("DataSetControllerImpl.updateRecords(..)"),

  /** The update field. */
  UPDATE_FIELD("DataSetControllerImpl.updateField(..)"),

  /** The import etl. */
  IMPORT_ETL("DataSetControllerImpl.etlImportDataset(..)"),

  /** The delete field schema. */
  DELETE_FIELD_SCHEMA("DatasetSchemaControllerImpl.deleteFieldSchema(..)"),

  /** The execute external integration. */
  EXECUTE_EXTERNAL_INTEGRATION("IntegrationControllerImpl.executeExternalIntegration(..)");



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
