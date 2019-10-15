package org.eea.interfaces.vo.ums.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The enum Access scope enum.
 */
public enum AccessScopeEnum {
  /**
   * Read access scope enum.
   */
  READ("READ"),
  /**
   * Update access scope enum.
   */
  UPDATE("UPDATE"),
  /**
   * Delete access scope enum.
   */
  DELETE("DELETE"),
  /**
   * Manage access scope enum.
   */
  MANAGE("MANAGE"),
  /**
   * Manage data access scope enum.
   */
  MANAGE_DATA("MANAGE_DATA"),
  /**
   * Release access scope enum.
   */
  RELEASE("RELEASE"),
  /**
   * Import access scope enum.
   */
  IMPORT("IMPORT"),
  /**
   * Export access scope enum.
   */
  EXPORT("EXPORT"),
  /**
   * Validate access scope enum.
   */
  VALIDATE("VALIDATE"),
  /**
   * Create access scope enum.
   */
  CREATE("CREATE");

  /**
   * Gets the scope.
   *
   * @return the scope
   */
  @JsonValue
  public String getScope() {
    return scope;
  }

  /** The scope. */
  private String scope;

  /**
   * Instantiates a new access scope enum.
   *
   * @param scope the scope
   */
  AccessScopeEnum(String scope) {
    this.scope = scope;
  }


}
