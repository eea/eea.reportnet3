package org.eea.interfaces.vo.dataset.enums;

/**
 * The Enum TypeDatasetEnum.
 */
public enum DatasetTypeEnum {

  /** The reporting. */
  REPORTING("REPORTING"),

  /** The design. */
  DESIGN("DESIGN"),

  /** The collection. */
  COLLECTION("COLLECTION"),

  /** The test. */
  TEST("TEST"),

  /** The eudataset. */
  EUDATASET("EUDATASET");


  /** The value. */
  private final String value;

  /**
   * Instantiates a new type error enum.
   *
   * @param value the value
   */
  DatasetTypeEnum(String value) {
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
