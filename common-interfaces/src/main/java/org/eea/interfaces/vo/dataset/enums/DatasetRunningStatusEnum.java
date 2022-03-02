package org.eea.interfaces.vo.dataset.enums;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * The Enum DatasetRunningStatusEnum.
 */
public enum DatasetRunningStatusEnum {

  /** The in queue. */
  IN_QUEUE("IN_QUEUE"),

  /** There was error in the validation process. */
  ERROR_IN_VALIDATION("ERROR_IN_VALIDATION"),

  /** There was error in the import process. */
  ERROR_IN_IMPORT("ERROR_IN_IMPORT"),

  /** The dataset is being imported. */
  IMPORTING("IMPORTING"),

  /** The dataset data has been imported. */
  IMPORTED("IMPORTED"),

  /** The dataset is being validated. */
  VALIDATING("VALIDATING"),

  /** The dataset has been validated. */
  VALIDATED("VALIDATED"),

  /** The snapshot restoring is in process. */
  RESTORING_SNAPSHOT("RESTORING_SNAPSHOT");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new dataset status enum.
   *
   * @param value the value
   */
  DatasetRunningStatusEnum(String value) {
    this.value = value;
  }


  /**
   * To string.
   *
   * @return the string
   */
  @Override
  @JsonValue
  public String toString() {
    return this.value;
  }

  /**
   * From value resource enum.
   *
   * @param value the value
   *
   * @return the resource enum
   */
  @JsonCreator
  public static DatasetRunningStatusEnum fromValue(String value) {
    return Arrays.stream(DatasetRunningStatusEnum.values()).filter(e -> e.value.equals(value))
        .findFirst().orElseThrow(IllegalStateException::new);
  }
}
