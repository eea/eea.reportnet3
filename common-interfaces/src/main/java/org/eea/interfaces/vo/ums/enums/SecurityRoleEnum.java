package org.eea.interfaces.vo.ums.enums;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The enum Security role enum.
 */
public enum SecurityRoleEnum {
  /**
   * Data custodian security role enum.
   */
  DATA_CUSTODIAN("DATA_CUSTODIAN"),
  /**
   * The editor with edit access.
   */
  EDITOR_WRITE("EDITOR_WRITE"),
  /**
   * The editor read-only access.
   */
  EDITOR_READ("EDITOR_READ"),
  /**
   * The lead reporter.
   */
  LEAD_REPORTER("LEAD_REPORTER"),
  /**
   * Data requester security role enum.
   */
  DATA_REQUESTER("DATA_REQUESTER"),
  /**
   * Data steward security role enum.
   */
  DATA_STEWARD("DATA_STEWARD"),
  /**
   * The reporter with edit access.
   */
  REPORTER_WRITE("REPORTER_WRITE"),
  /**
   * The reporter read-only access.
   */
  REPORTER_READ("REPORTER_READ"),
  /**
   * The reporter partitioned.
   */
  REPORTER_PARTITIONED("REPORTER_PARTITIONED"),
  /**
   * The national coordinator.
   */
  NATIONAL_COORDINATOR("NATIONAL_COORDINATOR");

  private final String role;

  private SecurityRoleEnum(String role) {
    this.role = role;
  }

  @Override
  @JsonValue
  public String toString() {
    return this.role;
  }

  /**
   * From value security role enum.
   *
   * @param value the value
   *
   * @return the security role enum
   */
  @JsonCreator
  public static SecurityRoleEnum fromValue(String value) {
    return Arrays.stream(SecurityRoleEnum.values()).filter(e -> e.role.equals(value)).findFirst()
        .get();
  }
}
