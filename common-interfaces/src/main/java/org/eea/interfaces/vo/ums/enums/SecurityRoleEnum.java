package org.eea.interfaces.vo.ums.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum SecurityRoleEnum {
  DATA_CUSTODIAN("DATA_CUSTODIAN"),
  DATA_PROVIDER("DATA_PROVIDER"),
  DATA_REQUESTER("DATA_REQUESTER"),
  DATA_STEWARD("DATA_STEWARD"),
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

  @JsonCreator
  public static SecurityRoleEnum fromValue(String value) {
    return Arrays.stream(SecurityRoleEnum.values()).filter(e -> e.role.equals(value)).findFirst()
        .get();
  }
}
