package org.eea.interfaces.vo.dataset.enums;

import java.util.Arrays;


/**
 * The Enum FileTypeEnum.
 */
public enum FileTypeEnum {


  /** The csv. */
  CSV("csv"),

  /** The xml. */
  XML("xml"),

  /** The xls. */
  XLS("xls"),

  /** The xlsx. */
  XLSX("xlsx"),

  /** The validations. */
  VALIDATIONS("validations");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new type entity enum.
   *
   * @param value the value
   */
  FileTypeEnum(String value) {
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


  /**
   * Gets the enum.
   *
   * @param value the value
   * @return the enum
   */
  public static FileTypeEnum getEnum(String value) {
    return Arrays.stream(FileTypeEnum.values()).filter(m -> m.value.equals(value)).findAny()
        .orElse(null);
  }
}
