package org.eea.interfaces.vo.dataset.enums;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The Enum DatasetStatusEnum.
 */
public enum DatasetStatusEnum {

  /** The final feedback. */
  FINAL_FEEDBACK("Final feedback"),

  /** The technically accept. */
  TECHNICALLY_ACCEPT("Technically accept"),

  /** The pending release. */
  PENDING_RELEASE("Pending release"),

  /** The correction requested. */
  CORRECTION_REQUESTED("Correction requested"),
  
  /** The released. */
  RELEASED("Released");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new dataset status enum.
   *
   * @param value the value
   */
  DatasetStatusEnum(String value) {
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
  public static DatasetStatusEnum fromValue(String value) {
    return Arrays.stream(DatasetStatusEnum.values()).filter(e -> e.value.equals(value)).findFirst()
        .orElseThrow(IllegalStateException::new);
  }
}
