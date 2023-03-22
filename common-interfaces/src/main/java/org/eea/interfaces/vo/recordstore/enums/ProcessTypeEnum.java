package org.eea.interfaces.vo.recordstore.enums;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * The Enum ProcessType.
 */
public enum ProcessTypeEnum {

  VALIDATION("VALIDATION"),

  IMPORT("IMPORT"),

  RELEASE_SNAPSHOT("RELEASE_SNAPSHOT"),

  RELEASE("RELEASE"),

  COPY_TO_EU_DATASET("COPY_TO_EU_DATASET"),

  RESTORE_REPORTING_DATASET("RESTORE_REPORTING_DATASET"),

  RESTORE_DESIGN_DATASET("RESTORE_DESIGN_DATASET"),

  COPY_REFERENCE_DATASET("COPY_REFERENCE_DATASET"),

  FILE_EXPORT("FILE_EXPORT");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new dataset status enum.
   *
   * @param value the value
   */
  ProcessTypeEnum(String value) {
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
  public static ProcessTypeEnum fromValue(String value) {
    return Arrays.stream(ProcessTypeEnum.values()).filter(e -> e.value.equals(value)).findFirst()
        .orElseThrow(IllegalStateException::new);
  }
}
