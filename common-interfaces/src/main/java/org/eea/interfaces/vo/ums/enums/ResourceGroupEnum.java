package org.eea.interfaces.vo.ums.enums;


/**
 * The enum Resource group enum. Allows to create groups
 */
public enum ResourceGroupEnum {
  /**
   * The dataflow lead reporter.
   */
  DATAFLOW_LEAD_REPORTER("Dataflow-%s-LEAD_REPORTER"),
  /**
   * Dataflow requester resource group enum.
   */
  DATAFLOW_REQUESTER("Dataflow-%s-DATA_REQUESTER"),
  /**
   * Dataflow steward resource group enum.
   */
  DATAFLOW_STEWARD("Dataflow-%s-DATA_STEWARD"),
  /**
   * Dataflow custodian resource group enum.
   */
  DATAFLOW_CUSTODIAN("Dataflow-%s-DATA_CUSTODIAN"),
  /**
   * The dataset lead reporter.
   */
  DATASET_LEAD_REPORTER("Dataset-%s-LEAD_REPORTER"),
  /**
   * Dataset requester resource group enum.
   */
  DATASET_REQUESTER("Dataset-%s-DATA_REQUESTER"),
  /**
   * Dataset steward resource group enum.
   */
  DATASET_STEWARD("Dataset-%s-DATA_STEWARD"),
  /**
   * Dataset custodian resource group enum.
   */
  DATASET_CUSTODIAN("Dataset-%s-DATA_CUSTODIAN"),

  /**
   * Dataschema custodian resource group enum.
   */
  DATASCHEMA_CUSTODIAN("Dataschema-%s-DATA_CUSTODIAN"),
  /**
   * Dataschema requester resource group enum.
   */
  DATASCHEMA_REQUESTER("Dataschema-%s-DATA_REQUESTER"),
  /**
   * Dataschema reporter resource group enum.
   */
  DATASCHEMA_REPORTER("Dataschema-%s-REPORTER"),

  /** The dataschema lead reporter. */
  DATASCHEMA_LEAD_REPORTER("Dataschema-%s-LEAD_REPORTER"),

  /** The datacollection custodian. */
  DATACOLLECTION_CUSTODIAN("DataCollection-%s-DATA_CUSTODIAN"),

  /** The datacollection lead_reporter. */
  DATACOLLECTION_LEAD_REPORTER("DataCollection-%s-DATA_LEAD_REPORTER"),

  /** The dataflow editor write. */
  DATAFLOW_EDITOR_WRITE("Dataflow-%s-EDITOR_WRITE"),

  /** The dataflow editor read. */
  DATAFLOW_EDITOR_READ("Dataflow-%s-EDITOR_READ"),

  /** The dataschema editor write. */
  DATASCHEMA_EDITOR_WRITE("Dataschema-%s-EDITOR_WRITE"),

  /** The dataschema editor read. */
  DATASCHEMA_EDITOR_READ("Dataschema-%s-EDITOR_READ"),

  /** The dataflow reporter read. */
  DATAFLOW_REPORTER("Dataflow-%s-REPORTER"),

  /** The dataset reporter write. */
  DATASET_REPORTER_WRITE("Dataset-%s-REPORTER_WRITE"),

  /** The dataset reporter read. */
  DATASET_REPORTER_READ("Dataset-%s-REPORTER_READ");


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
   * Gets group name.
   *
   * @param idEntity the id entity
   *
   * @return the group name
   */
  public String getGroupName(Long idEntity) {
    return String.format(this.expression, idEntity);
  }

  /**
   * From resource type and security role resource group enum.
   *
   * @param resourceTypeEnum the resource type enum
   * @param securityRoleEnum the security role enum
   *
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
