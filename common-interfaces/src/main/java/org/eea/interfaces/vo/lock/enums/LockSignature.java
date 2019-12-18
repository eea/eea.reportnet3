package org.eea.interfaces.vo.lock.enums;

/**
 * The Enum LockSignature.
 */
public enum LockSignature {

  /**
   * DataSetSnapshotControllerImpl.createSnapshot(..)
   */
  CREATE_SNAPSHOT("DataSetSnapshotControllerImpl.createSnapshot(..)"),

  /**
   * DataSetSnapshotControllerImpl.restoreSnapshot(..)
   */
  RESTORE_SNAPSHOT("DataSetSnapshotControllerImpl.restoreSnapshot(..)"),

  /**
   * DataSetControllerImpl.loadTableData(..)
   */
  LOAD_TABLE("DataSetControllerImpl.loadTableData(..)"),

  /**
   * ExecuteValidationCommand.execute(..)
   */
  EXECUTE_VALIDATION("ExecuteValidationCommand.execute(..)"),

  /**
   * The create schema snapshot.
   */
  CREATE_SCHEMA_SNAPSHOT("DataSetSnapshotControllerImpl.createSchemaSnapshot(..)"),

  /**
   * The restore schema snapshot.
   */
  RESTORE_SCHEMA_SNAPSHOT("DataSetSnapshotControllerImpl.restoreSchemaSnapshot(..)"),

  /**
   * The delete import table.
   */
  DELETE_IMPORT_TABLE("DataSetControllerImpl.deleteImportTable(..)");

  /**
   * The value.
   */
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
