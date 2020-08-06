package org.eea.interfaces.vo.dataflow.enums;

/**
 * The Enum FMEJobstatus.
 */
public enum FMEJobstatus {


  /** The submitted. */
  SUBMITTED("SUBMITTED"),
  /** The queued. */
  QUEUED("QUEUED"),
  /** The aborted. */
  ABORTED("ABORTED"),
  /** The success. */
  SUCCESS("SUCCESS"),
  /** The fme failure. */
  FME_FAILURE("FME_FAILURE"),
  /** The job failure. */
  JOB_FAILURE("JOB_FAILURE"),
  /** The pulled. */
  PULLED("PULLED");


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
