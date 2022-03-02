package org.eea.interfaces.vo.recordstore.enums;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The Enum ProcessStateEnum.
 */
public enum ProcessStatusEnum {

  /** The in queue. */
  IN_QUEUE("IN_QUEUE"),

  /** The in process. */
  IN_PROGRESS("IN_PROGRESS"),

  /** The canceled. */
  CANCELED("CANCELED"),

  /** The finished. */
  FINISHED("FINISHED");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new process state enum.
   *
   * @param value the value
   */
  ProcessStatusEnum(String value) {
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
   * From value.
   *
   * @param value the value
   * @return the process state enum
   */
  @JsonCreator
  public static ProcessStatusEnum fromValue(String value) {
    return Arrays.stream(ProcessStatusEnum.values()).filter(e -> e.value.equals(value)).findFirst()
        .orElseThrow(IllegalStateException::new);
  }
}
