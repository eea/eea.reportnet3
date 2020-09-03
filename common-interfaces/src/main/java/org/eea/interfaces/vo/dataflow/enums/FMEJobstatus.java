package org.eea.interfaces.vo.dataflow.enums;

/**
 * The Enum FMEJobstatus.
 */
public enum FMEJobstatus {

  /** The created. */
  CREATED("CREATED"),

  /** The aborted. */
  ABORTED("ABORTED"),

  /** The queued. */
  QUEUED("QUEUED"),

  /** The success. */
  SUCCESS("SUCCESS"),

  /** The job failure. */
  FAILURE("FAILURE");

  /** The value. */
  private final String value;

  /**
   * Instantiates a new FME jobstatus.
   *
   * @param value the value
   */
  FMEJobstatus(String value) {
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
