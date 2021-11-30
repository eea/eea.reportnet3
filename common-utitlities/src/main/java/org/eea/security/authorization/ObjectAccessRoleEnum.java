package org.eea.security.authorization;

/**
 * The enum Object access role enum. Allows to know if the user has access to the right entity
 */
public enum ObjectAccessRoleEnum {

  /** The dataflow lead reporter. */
  DATAFLOW_LEAD_REPORTER("ROLE_DATAFLOW-%s-LEAD_REPORTER"),

  /** The dataflow requester. */
  DATAFLOW_REQUESTER("ROLE_DATAFLOW-%s-DATA_REQUESTER"),

  /** The dataflow custodian. */
  DATAFLOW_CUSTODIAN("ROLE_DATAFLOW-%s-DATA_CUSTODIAN"),

  /** The dataflow observer. */
  DATAFLOW_OBSERVER("ROLE_DATAFLOW-%s-DATA_OBSERVER"),

  /** The dataflow reporter write. */
  DATAFLOW_REPORTER_WRITE("ROLE_DATAFLOW-%s-REPORTER_WRITE"),

  /** The dataflow reporter read. */
  DATAFLOW_REPORTER_READ("ROLE_DATAFLOW-%s-REPORTER_READ"),

  /** The dataflow national coordinator. */
  DATAFLOW_NATIONAL_COORDINATOR("ROLE_DATAFLOW-%s-NATIONAL_COORDINATOR"),

  /** The dataflow editor write. */
  DATAFLOW_EDITOR_WRITE("ROLE_DATAFLOW-%s-EDITOR_WRITE"),

  /** The dataflow editor read. */
  DATAFLOW_EDITOR_READ("ROLE_DATAFLOW-%s-EDITOR_READ"),

  /** The dataflow steward. */
  DATAFLOW_STEWARD("ROLE_DATAFLOW-%s-DATA_STEWARD"),

  /** The dataset lead reporter. */
  DATASET_LEAD_REPORTER("ROLE_DATASET-%s-LEAD_REPORTER"),

  /** The dataset requester. */
  DATASET_REQUESTER("ROLE_DATASET-%s-DATA_REQUESTER"),

  /** The dataset steward. */
  DATASET_STEWARD("ROLE_DATASET-%s-DATA_STEWARD"),

  /** The dataset custodian. */
  DATASET_CUSTODIAN("ROLE_DATASET-%s-DATA_CUSTODIAN"),

  /** The dataset observer. */
  DATASET_OBSERVER("ROLE_DATASET-%s-DATA_OBSERVER"),

  /** The dataset reporter write. */
  DATASET_REPORTER_WRITE("ROLE_DATASET-%s-REPORTER_WRITE"),

  /** The dataset reporter read. */
  DATASET_REPORTER_READ("ROLE_DATASET-%s-REPORTER_READ"),

  /** The dataset national coordinator. */
  DATASET_NATIONAL_COORDINATOR("ROLE_DATASET-%s-NATIONAL_COORDINATOR"),

  /** The dataschema editor write. */
  DATASCHEMA_EDITOR_WRITE("ROLE_DATASCHEMA-%s-EDITOR_WRITE"),

  /** The dataschema editor read. */
  DATASCHEMA_EDITOR_READ("ROLE_DATASCHEMA-%s-EDITOR_READ"),

  /** The dataschema steward. */
  DATASCHEMA_STEWARD("ROLE_DATASCHEMA-%s-DATA_STEWARD"),

  /** The dataschema custodian. */
  DATASCHEMA_CUSTODIAN("ROLE_DATASCHEMA-%s-DATA_CUSTODIAN"),

  /** The dataschema reporter read. */
  DATASCHEMA_REPORTER_READ("ROLE_DATASCHEMA-%s-REPORTER_READ"),

  /** The dataschema lead reporter. */
  DATASCHEMA_LEAD_REPORTER("ROLE_DATASCHEMA-%s-LEAD_REPORTER"),

  /** The dataschema national coordinator. */
  DATASCHEMA_NATIONAL_COORDINATOR("ROLE_DATASCHEMA-%s-NATIONAL_COORDINATOR"),

  /** The datacollection custodian. */
  DATACOLLECTION_CUSTODIAN("ROLE_DATACOLLECTION-%s-DATA_CUSTODIAN"),

  /** The datacollection observer. */
  DATACOLLECTION_OBSERVER("ROLE_DATACOLLECTION-%s-DATA_OBSERVER"),

  /** The datacollection steward. */
  DATACOLLECTION_STEWARD("ROLE_DATACOLLECTION-%s-DATA_STEWARD"),

  /** The datacollection lead reporter. */
  DATACOLLECTION_LEAD_REPORTER("ROLE_DATACOLLECTION-%s-LEAD_REPORTER"),

  /** The eudataset custodian. */
  EUDATASET_CUSTODIAN("ROLE_EUDATASET-%s-DATA_CUSTODIAN"),

  /** The eudataset steward. */
  EUDATASET_STEWARD("ROLE_EUDATASET-%s-DATA_STEWARD"),

  /** The eudataset observer. */
  EUDATASET_OBSERVER("ROLE_EUDATASET-%s-DATA_OBSERVER"),

  /** The testdataset custodian. */
  TESTDATASET_CUSTODIAN("ROLE_TESTDATASET-%s-DATA_CUSTODIAN"),

  /** The testdataset steward. */
  TESTDATASET_STEWARD("ROLE_TESTDATASET-%s-DATA_STEWARD"),

  /** The testdataset observer. */
  TESTDATASET_OBSERVER("ROLE_TESTDATASET-%s-DATA_OBSERVER"),

  /** The referencedataset observer. */
  REFERENCEDATASET_OBSERVER("ROLE_REFERENCEDATASET-%s-DATA_OBSERVER"),

  /** The referencedataset steward. */
  REFERENCEDATASET_STEWARD("ROLE_REFERENCEDATASET-%s-DATA_STEWARD"),

  /** The referencedataset custodian. */
  REFERENCEDATASET_CUSTODIAN("ROLE_REFERENCEDATASET-%s-DATA_CUSTODIAN");



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
