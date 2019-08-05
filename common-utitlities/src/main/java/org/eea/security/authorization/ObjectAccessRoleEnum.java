package org.eea.security.authorization;

/**
 * The enum Object access role enum.
 */
public enum ObjectAccessRoleEnum {
  /**
   * Dataflow provider object access role enum.
   */
  DATAFLOW_PROVIDER("ROLE_DATAFLOW_%s_PROVIDER"),
  /**
   * Dataflow requestor object access role enum.
   */
  DATAFLOW_REQUESTOR("ROLE_DATAFLOW_%s_REQUESTOR"),
  /**
   * Dataflow steward object access role enum.
   */
  DATAFLOW_STEWARD("ROLE_DATAFLOW_%s_REQUESTOR"),
  /**
   * Dataset provider object access role enum.
   */
  DATASET_PROVIDER("ROLE_DATASET_%s_PROVIDER"),
  /**
   * Dataset requestor object access role enum.
   */
  DATASET_REQUESTOR("ROLE_DATASET_%s_REQUESTOR"),
  /**
   * Dataset steward object access role enum.
   */
  DATASET_STEWARD("ROLE_DATASET_%s_REQUESTOR");


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
