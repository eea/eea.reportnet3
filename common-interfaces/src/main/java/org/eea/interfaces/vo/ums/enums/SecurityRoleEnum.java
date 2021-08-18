package org.eea.interfaces.vo.ums.enums;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The Enum SecurityRoleEnum.
 */
public enum SecurityRoleEnum {

  /** The data steward. */
  DATA_STEWARD("DATA_STEWARD"),

  /** The data custodian. */
  DATA_CUSTODIAN("DATA_CUSTODIAN"),

  /** The data observer. */
  DATA_OBSERVER("DATA_OBSERVER"),

  /** The data requester. */
  DATA_REQUESTER("DATA_REQUESTER"),

  /** The lead reporter. */
  LEAD_REPORTER("LEAD_REPORTER"),

  /** The reporter read. */
  REPORTER_READ("REPORTER_READ"),

  /** The reporter write. */
  REPORTER_WRITE("REPORTER_WRITE"),

  /** The editor read. */
  EDITOR_READ("EDITOR_READ"),

  /** The editor write. */
  EDITOR_WRITE("EDITOR_WRITE"),

  /** The reporter partitioned. */
  REPORTER_PARTITIONED("REPORTER_PARTITIONED"),

  /** The national coordinator. */
  NATIONAL_COORDINATOR("NATIONAL_COORDINATOR"),

  /** The admin. */
  ADMIN("ADMIN");

  /** The role. */
  private final String role;

  /**
   * Instantiates a new security role enum.
   *
   * @param role the role
   */
  private SecurityRoleEnum(String role) {
    this.role = role;
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  @JsonValue
  public String toString() {
    return this.role;
  }

  /**
   * From value.
   *
   * @param value the value
   * @return the security role enum
   */
  @JsonCreator
  public static SecurityRoleEnum fromValue(String value) {
    return Arrays.stream(SecurityRoleEnum.values()).filter(e -> e.role.equals(value)).findFirst()
        .orElse(null);
  }
}
