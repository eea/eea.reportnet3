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

  /** DataSetSnapshotControllerImpl.releaseSnapshot(..) */
  RELEASE_SNAPSHOT("DataSetSnapshotControllerImpl.releaseSnapshot(..)"),

  /** DataCollectionControllerImpl.createEmptyDataCollection(..) */
  CREATE_DATA_COLLECTION("DataCollectionControllerImpl.createEmptyDataCollection(..)"),

  /** DataSetControllerImpl.deleteImportTable(..) */
  DELETE_IMPORT_TABLE("DataSetControllerImpl.deleteImportTable(..)"),

  /** The delete dataset values. */
  DELETE_DATASET_VALUES("DataSetControllerImpl.deleteImportData(..)"),

  /** DataCollectionControllerImpl.updateDataCollection(..) */
  UPDATE_DATA_COLLECTION("DataCollectionControllerImpl.updateDataCollection(..)"),

  /** The copy dataset schema. */
  COPY_DATASET_SCHEMA("DataSetSchemaControllerImpl.copyDesignsFromDataflow(..)");

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
