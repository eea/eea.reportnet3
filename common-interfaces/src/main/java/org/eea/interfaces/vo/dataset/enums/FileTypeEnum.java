package org.eea.interfaces.vo.dataset.enums;

import java.util.Arrays;


public enum FileTypeEnum {


  CSV("csv"),

  XML("xml"),

  XLS("xls"),

  XLSX("xlsx"),

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


  public static FileTypeEnum getEnum(String value) {
    return Arrays.stream(FileTypeEnum.values()).filter(m -> m.value.equals(value)).findAny()
        .orElse(null);
  }
}
