package org.eea.interfaces.vo.ums.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

/**
 * The enum Security role enum.
 */
public enum SecurityRoleEnum {
  /**
   * Data custodian security role enum.
   */
  DATA_CUSTODIAN("DATA_CUSTODIAN"),
  /**
   * Data provider security role enum.
   */
  DATA_PROVIDER("DATA_PROVIDER"),
  /**
   * Data requester security role enum.
   */
  DATA_REQUESTER("DATA_REQUESTER"),
  /**
   * Data steward security role enum.
   */
  DATA_STEWARD("DATA_STEWARD"),
  /**
   * Delegate data provider security role enum.
   */
  DELEGATE_DATA_PROVIDER("DELEGATE_DATA_PROVIDER");

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
