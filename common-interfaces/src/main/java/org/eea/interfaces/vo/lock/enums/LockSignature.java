package org.eea.interfaces.vo.lock.enums;

/** The Enum LockSignature. */
public enum LockSignature {

  /** The empty. */
  EMPTY(""),

  /** DataSetSnapshotControllerImpl.createSnapshot(..) */
  CREATE_SNAPSHOT("DataSetSnapshotControllerImpl.createSnapshot(..)"),

  /** DataSetSnapshotControllerImpl.restoreSnapshot(..) */
  RESTORE_SNAPSHOT("DataSetSnapshotControllerImpl.restoreSnapshot(..)"),

  /** DataSetControllerImpl.loadTableData(..) */
  LOAD_TABLE("DataSetControllerImpl.loadTableData(..)"),

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

  /** DataSetControllerImpl.loadDatasetData(..) */
  LOAD_DATASET_DATA("DataSetControllerImpl.loadDatasetData(..)"),

  /** IntegrationControllerImpl.executeEUDatasetExport(..) */
  EXPORT_EU_DATASET("IntegrationControllerImpl.executeEUDatasetExport(..)"),

  /** The delete records. */
  DELETE_RECORDS("DataSetControllerImpl.deleteRecord(..)"),

  /** The insert records. */
  INSERT_RECORDS("DataSetControllerImpl.insertRecords(..)");


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
