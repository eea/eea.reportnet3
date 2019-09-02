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
  DATAFLOW_REQUESTOR("ROLE_DATAFLOW-%s-DATA_REQUESTOR"),
  /**
   * Dataflow steward object access role enum.
   */
  DATAFLOW_STEWARD("ROLE_DATAFLOW-%s-DATA_REQUESTOR"),
  /**
   * Dataflow custodian object access role enum.
   */
  DATAFLOW_CUSTODIAN("ROLE_DATAFLOW-%s-DATA_CUSTODIAN"),
  /**
   * Dataset provider object access role enum.
   */
  DATASET_PROVIDER("ROLE_DATASET-%s-DATA_PROVIDER"),
  /**
   * Dataset requestor object access role enum.
   */
  DATASET_REQUESTOR("ROLE_DATASET-%s-DATA_REQUESTOR"),
  /**
   * Dataset steward object access role enum.
   */
  DATASET_STEWARD("ROLE_DATASET-%s-DATA_REQUESTOR"),
  /**
   * Dataset custodian object access role enum.
   */
  DATASET_CUSTODIAN("ROLE_DATASET-%s-DATA_CUSTODIAN");


  private String expression;

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
