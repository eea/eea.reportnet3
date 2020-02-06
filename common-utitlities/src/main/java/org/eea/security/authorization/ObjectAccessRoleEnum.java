package org.eea.security.authorization;

/**
 * The enum Object access role enum.
 */
public enum ObjectAccessRoleEnum {
  /**
   * Dataflow provider object access role enum.
   */
  DATAFLOW_PROVIDER("ROLE_DATAFLOW-%s-DATA_PROVIDER"),
  /**
   * Dataflow requestor object access role enum.
   */
  DATAFLOW_REQUESTER("ROLE_DATAFLOW-%s-DATA_REQUESTER"),
  /**
   * Dataflow steward object access role enum.
   */
  DATAFLOW_STEWARD("ROLE_DATAFLOW-%s-DATA_STEWARD"),
  /**
   * Dataflow custodian object access role enum.
   */
  DATAFLOW_CUSTODIAN("ROLE_DATAFLOW-%s-DATA_CUSTODIAN"),
  /**
   * Dataset provider object access role enum.
   */
  DATASET_PROVIDER("ROLE_DATASET-%s-DATA_PROVIDER"),
  /**
   * Dataset requester object access role enum.
   */
  DATASET_REQUESTER("ROLE_DATASET-%s-DATA_REQUESTER"),
  /**
   * Dataset steward object access role enum.
   */
  DATASET_STEWARD("ROLE_DATASET-%s-DATA_STEWARD"),
  /**
   * Dataset custodian object access role enum.
   */
  DATASET_CUSTODIAN("ROLE_DATASET-%s-DATA_CUSTODIAN"),

  /** The dataschema custodian. */
  DATASCHEMA_CUSTODIAN("ROLE_DATASCHEMA-%s-DATA_CUSTODIAN"),

  /** The dataschema provider. */
  DATASCHEMA_PROVIDER("ROLE_DATASCHEMA-%s-DATA_PROVIDER"),

  /** The datacollection custodian. */
  DATACOLLECTION_CUSTODIAN("ROLE_DATACOLLECTION-%s-DATA_CUSTODIAN"),

  /** The datacollection provider. */
  DATACOLLECTION_PROVIDER("ROLE_DATACOLLECTION-%s-DATA_PROVIDER");


  /** The expression. */
  private String expression;

  /**
   * Instantiates a new object access role enum.
   *
   * @param expression the expression
   */
  private ObjectAccessRoleEnum(String expression) {
    this.expression = expression;
  }

  /**
   * Gets access role.
   *
   * @param idEntity the id entity
   *
   * @return the access role
   */
  public String getAccessRole(Long idEntity) {
    return String.format(this.expression, idEntity);
  }
}
