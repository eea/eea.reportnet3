package org.eea.lock.redis;

import lombok.Getter;

@Getter
public enum LockEnum {

  NATIONAL_COORDINATOR("NATIONAL_COORDINATOR"),
  TASK_SCHEDULER("TASK_SCHEDULER");

  private final String value;

  /**
   * Instantiates a new job status.
   *
   * @param value the value
   */
  LockEnum(String value) {
    this.value = value;
  }

}
