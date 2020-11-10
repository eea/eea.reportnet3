package org.eea.interfaces.vo.ums.enums;

/**
 * The Enum ResourceGroupEnum.
 */
public enum ResourceGroupEnum {

  // Dataflow

  /** The dataflow steward. */
  DATAFLOW_STEWARD("Dataflow-%s-DATA_STEWARD"),

  /** The dataflow custodian. */
  DATAFLOW_CUSTODIAN("Dataflow-%s-DATA_CUSTODIAN"),

  /** The dataflow requester. */
  DATAFLOW_REQUESTER("Dataflow-%s-DATA_REQUESTER"),

  /** The dataflow lead reporter. */
  DATAFLOW_LEAD_REPORTER("Dataflow-%s-LEAD_REPORTER"),

  /** The dataflow reporter read. */
  DATAFLOW_REPORTER_READ("Dataflow-%s-REPORTER_READ"),

  /** The dataflow reporter write. */
  DATAFLOW_REPORTER_WRITE("Dataflow-%s-REPORTER_WRITE"),

  /** The dataflow editor read. */
  DATAFLOW_EDITOR_READ("Dataflow-%s-EDITOR_READ"),

  /** The dataflow editor write. */
  DATAFLOW_EDITOR_WRITE("Dataflow-%s-EDITOR_WRITE"),

  // Dataset

  /** The dataset steward. */
  DATASET_STEWARD("Dataset-%s-DATA_STEWARD"),

  /** The dataset custodian. */
  DATASET_CUSTODIAN("Dataset-%s-DATA_CUSTODIAN"),

  /** The dataset requester. */
  DATASET_REQUESTER("Dataset-%s-DATA_REQUESTER"),

  /** The dataset lead reporter. */
  DATASET_LEAD_REPORTER("Dataset-%s-LEAD_REPORTER"),

  /** The dataset reporter read. */
  DATASET_REPORTER_READ("Dataset-%s-REPORTER_READ"),

  /** The dataset reporter write. */
  DATASET_REPORTER_WRITE("Dataset-%s-REPORTER_WRITE"),

  // DatasetSchema

  /** The dataschema custodian. */
  DATASCHEMA_CUSTODIAN("Dataschema-%s-DATA_CUSTODIAN"),

  /** The dataschema requester. */
  DATASCHEMA_REQUESTER("Dataschema-%s-DATA_REQUESTER"),

  /** The dataschema lead reporter. */
  DATASCHEMA_LEAD_REPORTER("Dataschema-%s-LEAD_REPORTER"),

  /** The dataschema reporter read. */
  DATASCHEMA_REPORTER_READ("Dataschema-%s-REPORTER_READ"),

  /** The dataschema editor read. */
  DATASCHEMA_EDITOR_READ("Dataschema-%s-EDITOR_READ"),

  /** The dataschema editor write. */
  DATASCHEMA_EDITOR_WRITE("Dataschema-%s-EDITOR_WRITE"),

  // DataCollection

  /** The datacollection custodian. */
  DATACOLLECTION_CUSTODIAN("DataCollection-%s-DATA_CUSTODIAN"),

  /** The datacollection lead reporter. */
  DATACOLLECTION_LEAD_REPORTER("DataCollection-%s-DATA_LEAD_REPORTER"),

  // EUDataset

  /** The eudataset custodian. */
  EUDATASET_CUSTODIAN("EUDataset-%s-DATA_CUSTODIAN");

  /** The expression. */
  private String expression;

  /**
   * Instantiates a new resource group enum.
   *
   * @param expression the expression
   */
  private ResourceGroupEnum(String expression) {
    this.expression = expression;
  }

  /**
   * Gets the group name.
   *
   * @param idEntity the id entity
   * @return the group name
   */
  public String getGroupName(Long idEntity) {
    return String.format(this.expression, idEntity);
  }

  /**
   * From resource type and security role.
   *
   * @param resourceTypeEnum the resource type enum
   * @param securityRoleEnum the security role enum
   * @return the resource group enum
   */
  public static ResourceGroupEnum fromResourceTypeAndSecurityRole(ResourceTypeEnum resourceTypeEnum,
      SecurityRoleEnum securityRoleEnum) {
    String resource = resourceTypeEnum.toString();
    String role = securityRoleEnum.toString();
    String resourceGroupExpresion =
        new StringBuilder(resource).append("-%s-").append(role).toString();
    ResourceGroupEnum[] values = ResourceGroupEnum.values();
    ResourceGroupEnum result = null;
    for (ResourceGroupEnum value : values) {
      if (resourceGroupExpresion.equals(value.expression)) {
        result = value;
        break;
      }
    }
    return result;
  }
}
